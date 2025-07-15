/**
 * モジュール名: RakutenBooksApiService
 * 作成者: 三浦寛生
 * 作成日: 2025/06/30
 * 概要:　楽天市場ランキングAPIとGoogle Books APIにアクセスするためのサービスです。
 * 書籍ランキングの取得と、その結果をGoogle Books APIで補完するロジックを担当します。
 * 履歴:
 * 2025/06/30 三浦寛生 新規作成
 */
package com.example.bookapp03.C6BookInformationManaging;

import android.util.Log;

import com.example.bookapp03.C7SearchManaging.GoogleBooksApiService;
import com.example.bookapp03.C4SearchProcessing.Book;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RakutenBooksApiService {

    private static final String TAG = "RakutenBooksApiService";
    private static final String RAKUTEN_ICHIBA_RANKING_API_URL = "https://app.rakuten.co.jp/services/api/IchibaItem/Ranking/20170628";

    private final String rakutenApplicationId;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final ExecutorService executorService;
    private final GoogleBooksApiService googleBooksApiService;


    public RakutenBooksApiService(OkHttpClient httpClient, Gson gson, String rakutenApplicationId, String googleBooksApiKey) {
        this.httpClient = httpClient;
        this.gson = gson;
        this.rakutenApplicationId = rakutenApplicationId;
        this.executorService = Executors.newFixedThreadPool(2);
        this.googleBooksApiService = new GoogleBooksApiService();
    }

    public interface RakutenBooksApiCallback {
        void onSuccess(List<Book> hotBooks);
        void onFailure(String errorMessage);
    }

    public void fetchRankingBooks(RakutenBooksApiCallback callback) {
        executorService.execute(() -> {
            try {
                HttpUrl.Builder urlBuilder = HttpUrl.parse(RAKUTEN_ICHIBA_RANKING_API_URL).newBuilder();
                urlBuilder.addQueryParameter("applicationId", rakutenApplicationId);
                urlBuilder.addQueryParameter("genreId", "200162");
                urlBuilder.addQueryParameter("hits", "10");
                urlBuilder.addQueryParameter("elements", "itemName,artistName,itemUrl,mediumImageUrl,isbn,salesDate");

                String url = urlBuilder.build().toString();
                Log.d(TAG, "Rakuten Ranking API URL: " + url);
                Request request = new Request.Builder().url(url).build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("楽天市場ランキングAPI呼び出し失敗: " + response.code() + " " + response.message());
                    }

                    String responseBody = response.body().string();
                    Log.d(TAG, "Rakuten Ichiba Ranking API Response: " + responseBody);

                    JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                    JsonArray itemsArray = jsonObject.getAsJsonArray("Items");

                    List<Book> hotBooks = new ArrayList<>();
                    if (itemsArray != null) {
                        for (JsonElement itemElement : itemsArray) {
                            JsonObject itemObject = itemElement.getAsJsonObject().getAsJsonObject("Item");
                            if (itemObject != null) {
                                String rakutenTitle = itemObject.has("itemName") ? itemObject.get("itemName").getAsString() : "タイトル不明";
                                String rakutenAuthor = itemObject.has("artistName") ? itemObject.get("artistName").getAsString() : "著者不明";
                                String rakutenItemUrl = itemObject.has("itemUrl") ? itemObject.get("itemUrl").getAsString() : null;
                                String rakutenMediumImageUrl = itemObject.has("mediumImageUrl") ? itemObject.get("mediumImageUrl").getAsString() : null;
                                String rakutenIsbn = itemObject.has("isbn") ? itemObject.get("isbn").getAsString() : null;
                                String rakutenSalesDate = itemObject.has("salesDate") ? itemObject.get("salesDate").getAsString() : null;

                                String[] exclusionKeywords = {"【楽天ブックス限定特典】", "（限定版）", "（特装版）"};
                                boolean shouldExclude = false;
                                for (String keyword : exclusionKeywords) {
                                    if (rakutenTitle.contains(keyword)) {
                                        shouldExclude = true;
                                        Log.d(TAG, "Excluding book due to keyword: '" + keyword + "' in title: " + rakutenTitle);
                                        break;
                                    }
                                }

                                if (shouldExclude) {
                                    continue;
                                }

                                Book book = new Book();
                                book.setTitle(rakutenTitle);
                                book.setAuthor(rakutenAuthor);
                                book.setRakutenItemUrl(rakutenItemUrl);
                                book.setRakutenLargeImageUrl(rakutenMediumImageUrl);
                                book.setThumbnailUrl(rakutenMediumImageUrl);
                                book.setIsbn(rakutenIsbn);
                                book.setPublishedDate(rakutenSalesDate);

                                Log.d(TAG, "Processing Rakuten Book (Before Google Search): Title=" + rakutenTitle + ", ISBN=" + rakutenIsbn + ", SalesDate=" + rakutenSalesDate);

                                Future<Void> googleSearchFuture = executorService.submit(new Callable<Void>() {
                                    @Override
                                    public Void call() throws Exception {
                                        fetchGoogleBooksDataForRakuenBookSync(book, rakutenTitle, rakutenAuthor, rakutenIsbn, rakutenSalesDate);
                                        return null;
                                    }
                                });
                                try {
                                    googleSearchFuture.get();
                                } catch (InterruptedException | ExecutionException e) {
                                    Log.e(TAG, "Google Books API sync error for '" + rakutenTitle + "': " + e.getMessage(), e);
                                }


                                if (book.getId() != null && !book.getId().isEmpty()) {
                                    hotBooks.add(book);
                                    Log.d(TAG, "Added Hot Book with Google ID: " + book.getId() + ", Title: " + book.getTitle());
                                } else {
                                    if (book.getThumbnailUrl() != null && !book.getThumbnailUrl().isEmpty()) {
                                        hotBooks.add(book);
                                        Log.w(TAG, "Added Hot Book without Google ID (using Rakuten image): " + book.getTitle() + " (ISBN: " + rakutenIsbn + ")");
                                    } else {
                                        Log.w(TAG, "Skipping book with no Google ID and no Rakuten image: " + book.getTitle() + " (ISBN: " + rakutenIsbn + ")");
                                    }
                                }
                            }
                        }
                    }
                    callback.onSuccess(hotBooks);
                }
            } catch (IOException e) {
                Log.e(TAG, "楽天市場ランキングAPI呼び出しエラー: " + e.getMessage(), e);
                callback.onFailure("話題の本の取得に失敗しました: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "楽天市場ランキングデータ処理エラー: " + e.getMessage(), e);
                callback.onFailure("話題の本の処理に失敗しました: " + e.getMessage());
            }
        });
    }

    /**
     * 楽天から取得した本に対して、Google Books APIで追加情報を検索し、Bookオブジェクトを更新します。
     * このメソッドは、GoogleBooksApiServiceを利用して検索し、結果をスコアリングします。
     *
     * @param book   更新対象のBookオブジェクト
     * @param title  楽天から取得した本のタイトル（Google Books APIの検索クエリ用）
     * @param author 楽天から取得した本の著者名（Google Books APIの検索クエリ用）
     * @param isbn   楽天から取得した本のISBN（Google Books APIの検索クエリ用、優先）
     * @param rakutenSalesDate 楽天から取得した本の発売日（出版年比較用）
     */
    private void fetchGoogleBooksDataForRakuenBookSync(Book book, String title, String author, String isbn, String rakutenSalesDate) {
        String googleQuery;
        String cleanedRakutenTitle = cleanRakutenTitle(title);


        try {
            if (isbn != null && !isbn.isEmpty() && !isbn.equals("null")) {
                googleQuery = "isbn:" + isbn;
                Log.d(TAG, "Google Books API: Searching by ISBN: " + isbn + " for title: " + cleanedRakutenTitle);
            } else {
                googleQuery = cleanedRakutenTitle;
                Log.d(TAG, "Google Books API: Searching by CLEANED TITLE ONLY: '" + cleanedRakutenTitle + "' -> Query: " + googleQuery);
            }

            List<Book> googleCandidateBooks = new ArrayList<>();
            final Object lock = new Object();
            Log.d(TAG, "googleQuery =" + googleQuery);

            googleBooksApiService.searchBooks(googleQuery, new GoogleBooksApiService.SearchCallback() {
                @Override
                public void onSearchResultsReceived(List<Book> results) {
                    synchronized (lock) {
                        googleCandidateBooks.addAll(results);
                        lock.notify();
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "GoogleBooksApiService search failed: " + errorMessage);
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            });

            synchronized (lock) {
                try {
                    lock.wait(TimeUnit.SECONDS.toMillis(10));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Log.e(TAG, "Google Books API search wait interrupted: " + e.getMessage());
                }
            }

            if (!googleCandidateBooks.isEmpty()) {
                Book bestMatchBook = null;
                int highestScore = -1;
                final int MIN_ACCEPTABLE_SCORE = 60;



                Log.d(TAG, "Evaluating Google Candidates for '" + cleanedRakutenTitle + "': " + googleCandidateBooks.size() + " books found.");

                for (Book googleBookCandidate : googleCandidateBooks) {
                    int currentScore = 0;

                    if (isbn != null && !isbn.isEmpty() && !isbn.equals("null") &&
                            googleBookCandidate.getIsbn() != null && !googleBookCandidate.getIsbn().isEmpty() &&
                            googleBookCandidate.getIsbn().equals(isbn)) {
                        currentScore += 1000;
                        Log.d(TAG, "Score: ISBN match for " + cleanedRakutenTitle + ". Score: " + currentScore);
                    }

                    String googleTitle = googleBookCandidate.getTitle();
                    String normalizedGoogleTitle = normalizeTitle(googleTitle);
                    String normalizedRakutenTitle = normalizeTitle(cleanedRakutenTitle);

                    Log.d(TAG, "Comparing Titles: Rakuten Cleaned='" + cleanedRakutenTitle + "', Google Candidate='" + googleTitle + "'");
                    Log.d(TAG, "Normalized Titles: Rakuten='" + normalizedRakutenTitle + "', Google Candidate='" + normalizedGoogleTitle + "'");

                    if (normalizedGoogleTitle.equals(normalizedRakutenTitle)) {
                        currentScore += 300;
                        Log.d(TAG, "Score: Exact (normalized) title match for " + cleanedRakutenTitle + ". Score: " + currentScore);
                    } else {
                        boolean googleContainsRakuten = normalizedGoogleTitle.contains(normalizedRakutenTitle);
                        boolean rakutenContainsGoogle = normalizedRakutenTitle.contains(normalizedGoogleTitle);

                        if (googleContainsRakuten) {
                            currentScore += 150;
                            Log.d(TAG, "Score: Google title CONTAINS Rakuten title for " + cleanedRakutenTitle + ". Score: " + currentScore);
                        } else if (rakutenContainsGoogle) {
                            currentScore += 100;
                            Log.d(TAG, "Score: Rakuten title CONTAINED BY Google title for " + cleanedRakutenTitle + ". Score: " + currentScore);
                        } else {
                            Log.d(TAG, "Score: No significant title match for " + cleanedRakutenTitle + ". Score: " + currentScore);
                        }
                    }

                    if (rakutenSalesDate != null && !rakutenSalesDate.isEmpty() &&
                            googleBookCandidate.getPublishedDate() != null && !googleBookCandidate.getPublishedDate().isEmpty()) {
                        try {
                            String rakutenYearStr = rakutenSalesDate.split("年")[0];
                            int rakutenYear = Integer.parseInt(rakutenYearStr);
                            int googleYear = Integer.parseInt(googleBookCandidate.getPublishedDate().substring(0, Math.min(googleBookCandidate.getPublishedDate().length(), 4)));

                            if (rakutenYear == googleYear) {
                                currentScore += 100;
                                Log.d(TAG, "Score: Publishing year match for " + cleanedRakutenTitle + ". Score: " + currentScore);
                            } else {
                            }
                        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                            Log.w(TAG, "Could not parse publishing year for " + cleanedRakutenTitle + ": " + e.getMessage());
                        }
                    }
                    Log.d(TAG, "Candidate Book: '" + googleBookCandidate.getTitle() + "' (Google ID: " + googleBookCandidate.getId() + "), Total Score: " + currentScore);
                    if (currentScore > highestScore) {
                        highestScore = currentScore;
                        bestMatchBook = googleBookCandidate;
                        Log.d(TAG, "New Best Match Candidate: '" + googleBookCandidate.getTitle() + "' with Score: " + highestScore);
                    }
                }
                if (bestMatchBook != null && highestScore >= MIN_ACCEPTABLE_SCORE) {
                    book.setId(bestMatchBook.getId());
                    book.setTitle(bestMatchBook.getTitle());
                    book.setAuthor(bestMatchBook.getAuthor());
                    book.setDescription(bestMatchBook.getDescription());
                    book.setInfoLink(bestMatchBook.getInfoLink());
                    book.setThumbnailUrl(bestMatchBook.getThumbnailUrl());
                    book.setCategories(bestMatchBook.getCategories());
                    book.setPublishedDate(bestMatchBook.getPublishedDate());

                    if (bestMatchBook.getIsbn() != null && !bestMatchBook.getIsbn().isEmpty() &&
                            (book.getIsbn() == null || book.getIsbn().isEmpty() || book.getIsbn().equals("null"))) {
                        book.setIsbn(bestMatchBook.getIsbn());
                    }

                    Log.d(TAG, "Google Books API: Best match SELECTED for '" + cleanedRakutenTitle + "'. ID: " + book.getId() + ", Final Score: " + highestScore);
                } else {
                    Log.d(TAG, "Google Books API: No suitable match found (score too low or no candidates) for query: " + googleQuery + " (Title: " + cleanedRakutenTitle + "). Keeping Rakuten data.");
                    book.setId(null);
                    book.setDescription(null);
                    book.setInfoLink(null);
                    book.setCategories(null);
                }
            } else {
                Log.d(TAG, "Google Books API: No items found for query: " + googleQuery + " (Title: " + cleanedRakutenTitle + "). Keeping Rakuten data.");
                book.setId(null);
                book.setDescription(null);
                book.setInfoLink(null);
                book.setCategories(null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error (Google secondary search): " + e.getMessage(), e);
        }
    }

    /**
     * 楽天から取得したタイトル文字列から、検索に不要な情報を除去し正規化します。
     */
    private String cleanRakutenTitle(String title) {
        if (title == null) return "";
        String cleaned = title;
        cleaned = cleaned.replaceAll("\\（.*?\\）|\\(.*?\\)", "");
        cleaned = cleaned.replaceAll("【.*?】", "");
        cleaned = cleaned.replaceAll("\\[.*?\\]", "");
        cleaned = cleaned.replaceAll("　", " ");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        Log.d(TAG, "Cleaned Rakuten Title: Original='" + title + "', Cleaned='" + cleaned + "'");
        return cleaned;
    }

    /**
     * タイトル文字列を正規化します。
     * 今回の設計ではGoogle APIに投げるクエリに使う`normalizeTitle`はシンプルにし、
     * スコアリングでの比較は柔軟に行う。
     */
    private String normalizeTitle(String title) {
        if (title == null) return "";
        return title.toLowerCase(java.util.Locale.ROOT).trim();
    }

    /**
     * 著者名文字列を正規化します。
     */
    private String normalizeAuthor(String author) {
        if (author == null) return "";
        return author.toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[\\s　-]+", "")
                .trim();
    }

    /**
     * このサービスで使用されているExecutorServiceをシャットダウンします。
     * アプリケーションが終了する際に呼び出す必要があります。
     * これにより、バックグラウンドスレッドが適切に終了し、リソースリークを防ぎます。
     */
    public void shutdown() {
        executorService.shutdown();
        googleBooksApiService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        Log.d(TAG, "RakutenBooksApiService ExecutorService shut down.");
    }
}