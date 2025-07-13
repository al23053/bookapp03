package com.example.bookapp03.C6BookInformationManaging;

import android.util.Log;

import com.example.bookapp03.C7SearchManaging.GoogleBooksApiService; // GoogleBooksApiService をインポート
import com.example.bookapp03.model.Book;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable; // 追加
import java.util.concurrent.ExecutionException; // 追加
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future; // 追加
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import androidx.annotation.NonNull;

/**
 * 楽天市場ランキングAPIとGoogle Books APIにアクセスするためのサービスです。
 * 書籍ランキングの取得と、その結果をGoogle Books APIで補完するロジックを担当します。
 */
public class RakutenBooksApiService {

    private static final String TAG = "RakutenBooksApiService";
    private static final String RAKUTEN_ICHIBA_RANKING_API_URL = "https://app.rakuten.co.jp/services/api/IchibaItem/Ranking/20170628";
    // GOOGLE_BOOKS_API_BASE_URL は GoogleBooksApiService が管理するため、ここでは不要になる

    private final String rakutenApplicationId;
    // private final String googleBooksApiKey; // GoogleBooksApiService が管理するため、ここでは不要になる
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final ExecutorService executorService;
    private final GoogleBooksApiService googleBooksApiService; // ★★★ 追加: GoogleBooksApiService のインスタンス ★★★


    public RakutenBooksApiService(OkHttpClient httpClient, Gson gson, String rakutenApplicationId, String googleBooksApiKey) {
        this.httpClient = httpClient;
        this.gson = gson;
        this.rakutenApplicationId = rakutenApplicationId;
        // this.googleBooksApiKey = googleBooksApiKey; // 不要
        this.executorService = Executors.newFixedThreadPool(2);
        this.googleBooksApiService = new GoogleBooksApiService(); // ★★★ GoogleBooksApiService を初期化 ★★★
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
                urlBuilder.addQueryParameter("genreId", "200162"); // 「本・雑誌・コミック」ジャンルID
                urlBuilder.addQueryParameter("hits", "10"); // 取得する書籍数を10件に制限
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

                                // 除外したいキーワードを定義
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
                                    continue; // この本はスキップして次の本へ
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

                                // Google Books APIで追加情報を検索し、Bookオブジェクトを更新 (Callableを使って同期的に呼び出し)
                                Future<Void> googleSearchFuture = executorService.submit(new Callable<Void>() {
                                    @Override
                                    public Void call() throws Exception {
                                        fetchGoogleBooksDataForRakuenBookSync(book, rakutenTitle, rakutenAuthor, rakutenIsbn, rakutenSalesDate);
                                        return null;
                                    }
                                });
                                try {
                                    googleSearchFuture.get(); // 処理が終わるまで待機
                                } catch (InterruptedException | ExecutionException e) {
                                    Log.e(TAG, "Google Books API sync error for '" + rakutenTitle + "': " + e.getMessage(), e);
                                    // エラーが発生しても、既存の楽天データを表示するために続行
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
        String cleanedRakutenTitle = cleanRakutenTitle(title); // 楽天タイトルを前処理


        try {
            if (isbn != null && !isbn.isEmpty() && !isbn.equals("null")) {
                googleQuery = "isbn:" + isbn;
                Log.d(TAG, "Google Books API: Searching by ISBN: " + isbn + " for title: " + cleanedRakutenTitle);
            } else {
                // ★★★ ここを完全にタイトルのみの検索に変更 ★★★
                googleQuery = cleanedRakutenTitle;
                Log.d(TAG, "Google Books API: Searching by CLEANED TITLE ONLY: '" + cleanedRakutenTitle + "' -> Query: " + googleQuery);
                // 以前の inauthor: を含むロジックは削除されます
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

            // ここからベストマッチの選定ロジックを修正・強化します
            if (!googleCandidateBooks.isEmpty()) {
                Book bestMatchBook = null;
                int highestScore = -1; // スコアを負の値から始めることで、0点の候補をより厳しく扱う
                final int MIN_ACCEPTABLE_SCORE = 60; // ★★★ 新規追加: Googleの情報を採用するための最低限のスコア閾値 ★★★
                // 例: タイトル部分一致(50点) + 著者一致(150点) -> 200点
                // もしくは、タイトル部分一致(50点) + 出版年一致(100点) -> 150点
                // これだと部分一致だけでも採用されてしまうので、例えば
                // 完全一致タイトルなら100以上、部分一致なら60以上など、調整が必要
                // ここでは仮に60と設定。状況に合わせて調整してください。


                Log.d(TAG, "Evaluating Google Candidates for '" + cleanedRakutenTitle + "': " + googleCandidateBooks.size() + " books found.");

                for (Book googleBookCandidate : googleCandidateBooks) {
                    int currentScore = 0;

                    // 1. ISBNの一致度 (最も重要: 1000点)
                    // ISBNが一致すれば、他の条件に関わらず最高のスコアを与える
                    if (isbn != null && !isbn.isEmpty() && !isbn.equals("null") &&
                            googleBookCandidate.getIsbn() != null && !googleBookCandidate.getIsbn().isEmpty() &&
                            googleBookCandidate.getIsbn().equals(isbn)) {
                        currentScore += 1000;
                        Log.d(TAG, "Score: ISBN match for " + cleanedRakutenTitle + ". Score: " + currentScore);
                    }

                    // 2. タイトルの一致度
                    String googleTitle = googleBookCandidate.getTitle();
                    String normalizedGoogleTitle = normalizeTitle(googleTitle);
                    String normalizedRakutenTitle = normalizeTitle(cleanedRakutenTitle);

                    Log.d(TAG, "Comparing Titles: Rakuten Cleaned='" + cleanedRakutenTitle + "', Google Candidate='" + googleTitle + "'");
                    Log.d(TAG, "Normalized Titles: Rakuten='" + normalizedRakutenTitle + "', Google Candidate='" + normalizedGoogleTitle + "'");

                    // ★★★ タイトル一致のスコアリングを調整 ★★★
                    if (normalizedGoogleTitle.equals(normalizedRakutenTitle)) {
                        currentScore += 300; // 完全一致は非常に高いスコア
                        Log.d(TAG, "Score: Exact (normalized) title match for " + cleanedRakutenTitle + ". Score: " + currentScore);
                    } else {
                        // より厳密な部分一致チェック
                        // Googleタイトルが楽天タイトルを完全に含んでいるか
                        boolean googleContainsRakuten = normalizedGoogleTitle.contains(normalizedRakutenTitle);
                        // 楽天タイトルがGoogleタイトルを完全に含んでいるか (Googleタイトルが短縮版の場合など)
                        boolean rakutenContainsGoogle = normalizedRakutenTitle.contains(normalizedGoogleTitle);

                        if (googleContainsRakuten) {
                            currentScore += 150; // Googleタイトルに楽天タイトルが完全に含まれる
                            Log.d(TAG, "Score: Google title CONTAINS Rakuten title for " + cleanedRakutenTitle + ". Score: " + currentScore);
                        } else if (rakutenContainsGoogle) {
                            currentScore += 100; // 楽天タイトルにGoogleタイトルが完全に含まれる
                            Log.d(TAG, "Score: Rakuten title CONTAINED BY Google title for " + cleanedRakutenTitle + ". Score: " + currentScore);
                        } else {
                            // ここで、タイトルが部分的にでも一致しない場合は、タイトルに関するスコアは加算しない
                            Log.d(TAG, "Score: No significant title match for " + cleanedRakutenTitle + ". Score: " + currentScore);
                        }
                    }




                    // 4. 出版年の一致度
                    if (rakutenSalesDate != null && !rakutenSalesDate.isEmpty() &&
                            googleBookCandidate.getPublishedDate() != null && !googleBookCandidate.getPublishedDate().isEmpty()) {
                        try {
                            String rakutenYearStr = rakutenSalesDate.split("年")[0];
                            int rakutenYear = Integer.parseInt(rakutenYearStr);
                            // Google Books APIの publishedDate は "YYYY-MM-DD" 形式の場合もあるので、最初の4文字で年を取得
                            int googleYear = Integer.parseInt(googleBookCandidate.getPublishedDate().substring(0, Math.min(googleBookCandidate.getPublishedDate().length(), 4)));

                            if (rakutenYear == googleYear) {
                                currentScore += 100;
                                Log.d(TAG, "Score: Publishing year match for " + cleanedRakutenTitle + ". Score: " + currentScore);
                            } else {
                                // 出版年が大きくずれている場合、ペナルティを与えることも検討可能
                                // currentScore -= Math.abs(rakutenYear - googleYear) * 5; // ずれが大きいほどペナルティ
                            }
                        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                            Log.w(TAG, "Could not parse publishing year for " + cleanedRakutenTitle + ": " + e.getMessage());
                        }
                    }

                    Log.d(TAG, "Candidate Book: '" + googleBookCandidate.getTitle() + "' (Google ID: " + googleBookCandidate.getId() + "), Total Score: " + currentScore);

                    // ベストマッチ候補を更新
                    if (currentScore > highestScore) {
                        highestScore = currentScore;
                        bestMatchBook = googleBookCandidate;
                        Log.d(TAG, "New Best Match Candidate: '" + googleBookCandidate.getTitle() + "' with Score: " + highestScore);
                    }
                }

                // ★★★ 最終的なベストマッチ採用条件の強化 ★★★
                // ISBNが一致したか、または設定した最低スコア閾値を超えている場合のみGoogleの情報を採用
                // ISBNマッチはスコア1000なので、MIN_ACCEPTABLE_SCOREより常に高くなる
                if (bestMatchBook != null && highestScore >= MIN_ACCEPTABLE_SCORE) { // ISBN一致 OR 最低スコア達成
                    // 最もスコアの高いGoogle Books APIの情報を元のBookオブジェクトに反映
                    book.setId(bestMatchBook.getId());
                    book.setTitle(bestMatchBook.getTitle()); // Googleのタイトルを優先
                    book.setAuthor(bestMatchBook.getAuthor()); // Googleの著者を優先
                    book.setDescription(bestMatchBook.getDescription());
                    book.setInfoLink(bestMatchBook.getInfoLink());
                    book.setThumbnailUrl(bestMatchBook.getThumbnailUrl());
                    book.setCategories(bestMatchBook.getCategories());
                    book.setPublishedDate(bestMatchBook.getPublishedDate());

                    // Googleから有効なISBNが取れた場合、楽天のISBNを上書きする (楽天のISBNがnull/空の場合のみ)
                    if (bestMatchBook.getIsbn() != null && !bestMatchBook.getIsbn().isEmpty() &&
                            (book.getIsbn() == null || book.getIsbn().isEmpty() || book.getIsbn().equals("null"))) {
                        book.setIsbn(bestMatchBook.getIsbn());
                    }

                    Log.d(TAG, "Google Books API: Best match SELECTED for '" + cleanedRakutenTitle + "'. ID: " + book.getId() + ", Final Score: " + highestScore);
                } else {
                    // 最適なGoogle Booksの候補が見つからなかった、またはスコアが低すぎた場合
                    // 楽天から取得した元の情報をそのまま利用する (bookオブジェクトは既に楽天データで初期化されているため、変更しない)
                    Log.d(TAG, "Google Books API: No suitable match found (score too low or no candidates) for query: " + googleQuery + " (Title: " + cleanedRakutenTitle + "). Keeping Rakuten data.");
                    // 必要であれば、bookのIDやdescriptionなどをnullに戻す（Googleから取得できなかったことを明示）
                    // ただし、bookオブジェクトはすでに楽天情報で初期化されているので、ここではそのままにするのが適切
                    book.setId(null); // Google IDは設定しない
                    book.setDescription(null); // Googleから取れなかった場合は説明をクリア
                    book.setInfoLink(null); // Googleから取れなかった場合は情報リンクをクリア
                    book.setCategories(null); // Googleから取れなかった場合はカテゴリをクリア

                    // タイトルと著者名は楽天のものをそのまま使う
                    // book.getTitle(), book.getAuthor() は楽天で初期化されている
                }
            } else {
                // Google Books APIが何も候補を返さなかった場合
                Log.d(TAG, "Google Books API: No items found for query: " + googleQuery + " (Title: " + cleanedRakutenTitle + "). Keeping Rakuten data.");
                book.setId(null); // Google IDは設定しない
                book.setDescription(null); // Googleから取れなかった場合は説明をクリア
                book.setInfoLink(null); // Googleから取れなかった場合は情報リンクをクリア
                book.setCategories(null); // Googleから取れなかった場合はカテゴリをクリア
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error (Google secondary search): " + e.getMessage(), e);
        }
    }

    /**
     * 楽天から取得したタイトル文字列から、検索に不要な情報を除去し正規化します。
     * 例: "ONE PIECE 112 (ジャンプコミック...)" -> "ONE PIECE 112"
     * 例: "【楽天ブックス限定特典】キングダム 76" -> "キングダム 76"
     */
    private String cleanRakutenTitle(String title) {
        if (title == null) return "";
        String cleaned = title;

        // 丸括弧内のテキストを除去 (例: (ジャンプコミック...))
        cleaned = cleaned.replaceAll("\\（.*?\\）|\\(.*?\\)", ""); // 全角半角の括弧に対応

        // 【】内のテキストを除去 (例: 【楽天ブックス限定特典】)
        cleaned = cleaned.replaceAll("【.*?】", "");

        // 半角の角括弧 [] 内のテキストを除去する正規表現を追加
        cleaned = cleaned.replaceAll("\\[.*?\\]", ""); // 半角の角括弧に対応

        // 全角スペースを半角スペースに変換
        cleaned = cleaned.replaceAll("　", " ");
        // 複数半角スペースを1つにまとめ、前後の半角スペースをトリミング
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
        // ここでは単に小文字化してトリミングするのみ。
        // Google Books APIが通常返すタイトルと合わせるため、スペースや記号の除去はしない
        return title.toLowerCase(java.util.Locale.ROOT).trim();
    }

    /**
     * 著者名文字列を正規化します。
     */
    private String normalizeAuthor(String author) {
        if (author == null) return "";
        return author.toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[\\s　-]+", "") // スペースやハイフンを除去
                .trim();
    }

    // parseGoogleBookJson メソッドは GoogleBooksApiService が担当するため、RakutenBooksApiService からは削除

    /**
     * このサービスで使用されているExecutorServiceをシャットダウンします。
     * アプリケーションが終了する際に呼び出す必要があります。
     * これにより、バックグラウンドスレッドが適切に終了し、リソースリークを防ぎます。
     */
    public void shutdown() {
        executorService.shutdown();
        googleBooksApiService.shutdown(); // ★★★ GoogleBooksApiService もシャットダウン ★★★
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