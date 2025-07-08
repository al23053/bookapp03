package com.example.bookapp03.service;

import android.util.Log;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 楽天市場ランキングAPIとGoogle Books APIにアクセスするためのサービスです。
 * 書籍ランキングの取得と、その結果をGoogle Books APIで補完するロジックを担当します。
 */
public class RakutenBooksApiService {

    private static final String TAG = "RakutenBooksApiService";
    private static final String RAKUTEN_ICHIBA_RANKING_API_URL = "https://app.rakuten.co.jp/services/api/IchibaItem/Ranking/20170628";
    private static final String GOOGLE_BOOKS_API_BASE_URL = "https://www.googleapis.com/books/v1/volumes";

    private final String rakutenApplicationId;
    private final String googleBooksApiKey;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final ExecutorService executorService;

    public RakutenBooksApiService(OkHttpClient httpClient, Gson gson, String rakutenApplicationId, String googleBooksApiKey) {
        this.httpClient = httpClient;
        this.gson = gson;
        this.rakutenApplicationId = rakutenApplicationId;
        this.googleBooksApiKey = googleBooksApiKey;
        this.executorService = Executors.newFixedThreadPool(2);
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
                urlBuilder.addQueryParameter("elements", "itemName,artistName,itemUrl,mediumImageUrl,isbn"); // ISBNも取得

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

                                Book book = new Book();
                                book.setTitle(rakutenTitle);
                                book.setAuthor(rakutenAuthor);
                                book.setRakutenItemUrl(rakutenItemUrl);
                                book.setRakutenLargeImageUrl(rakutenMediumImageUrl);
                                book.setThumbnailUrl(rakutenMediumImageUrl); // 最初は楽天の画像URLを設定
                                book.setIsbn(rakutenIsbn);

                                Log.d(TAG, "Processing Rakuten Book (Before Google Search): Title=" + rakutenTitle + ", ISBN=" + rakutenIsbn);

                                // Google Books APIのデータ取得を同期的に行う
                                // fetchGoogleBooksDataForRakuenBookSync 内で book.setId(googleId) が呼び出される
                                fetchGoogleBooksDataForRakuenBookSync(book, rakutenTitle, rakutenAuthor, rakutenIsbn);

                                // Google Books APIのIDがセットされたことを確認してから追加
                                if (book.getId() != null && !book.getId().isEmpty()) {
                                    hotBooks.add(book);
                                    Log.d(TAG, "Added Hot Book with Google ID: " + book.getId() + ", Title: " + book.getTitle());
                                } else {
                                    Log.w(TAG, "Skipping book with no Google ID after secondary search: " + book.getTitle() + " (ISBN: " + rakutenIsbn + ")");
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
     * このメソッドは、親メソッドのExecutorService内部で同期的に呼び出されるため、UIスレッドはブロックしません。
     *
     * @param book   更新対象のBookオブジェクト
     * @param title  楽天から取得した本のタイトル（Google Books APIの検索クエリ用）
     * @param author 楽天から取得した本の著者名（Google Books APIの検索クエリ用）
     * @param isbn   楽天から取得した本のISBN（Google Books APIの検索クエリ用、優先）
     */
    private void fetchGoogleBooksDataForRakuenBookSync(Book book, String title, String author, String isbn) {
        try {
            String googleQuery;
            if (isbn != null && !isbn.isEmpty() && !isbn.equals("null")) {
                googleQuery = "isbn:" + isbn;
                Log.d(TAG, "Google Books API: Searching by ISBN: " + isbn);
            } else {
                googleQuery = URLEncoder.encode(title + " " + author, "UTF-8");
                Log.d(TAG, "Google Books API: Searching by Title+Author: " + title + " " + author);
            }

            HttpUrl.Builder googleUrlBuilder = HttpUrl.parse(GOOGLE_BOOKS_API_BASE_URL).newBuilder();
            googleUrlBuilder.addQueryParameter("q", googleQuery);
            googleUrlBuilder.addQueryParameter("key", googleBooksApiKey);
            googleUrlBuilder.addQueryParameter("maxResults", "1"); // 検索結果を1件に絞る

            String googleBooksApiUrl = googleUrlBuilder.build().toString();
            Log.d(TAG, "Google Books API URL: " + googleBooksApiUrl);
            Request googleRequest = new Request.Builder().url(googleBooksApiUrl).build();

            try (Response googleResponse = httpClient.newCall(googleRequest).execute()) { // .execute() で同期的に呼び出す
                if (googleResponse.isSuccessful() && googleResponse.body() != null) {
                    String googleResponseBody = googleResponse.body().string();
                    Log.d(TAG, "Google Books API Raw Response for '" + title + "': " + googleResponseBody);

                    JsonObject googleJson = JsonParser.parseString(googleResponseBody).getAsJsonObject();
                    JsonArray googleItems = googleJson.getAsJsonArray("items");

                    if (googleItems != null && googleItems.size() > 0) {
                        JsonObject firstItem = googleItems.get(0).getAsJsonObject();
                        JsonObject volumeInfo = firstItem.getAsJsonObject("volumeInfo");

                        String googleId = firstItem.has("id") ? firstItem.get("id").getAsString() : null;
                        String infoLink = volumeInfo.has("infoLink") ? volumeInfo.get("infoLink").getAsString() : null;
                        String googleThumbnailUrl = null;
                        if (volumeInfo.has("imageLinks")) {
                            JsonObject imageLinks = volumeInfo.getAsJsonObject("imageLinks");
                            if (imageLinks.has("extraLarge")) {
                                googleThumbnailUrl = imageLinks.get("extraLarge").getAsString();
                            } else if (imageLinks.has("large")) {
                                googleThumbnailUrl = imageLinks.get("large").getAsString();
                            } else if (imageLinks.has("medium")) {
                                googleThumbnailUrl = imageLinks.get("medium").getAsString();
                            } else if (imageLinks.has("small")) {
                                googleThumbnailUrl = imageLinks.get("small").getAsString();
                            } else if (imageLinks.has("thumbnail")) {
                                googleThumbnailUrl = imageLinks.get("thumbnail").getAsString();
                            }
                        }

                        // ここで book の ID とサムネイルURLを更新する
                        book.setId(googleId); // ここでGoogle Books APIのIDをセット
                        book.setInfoLink(infoLink);
                        if (googleThumbnailUrl != null) {
                            book.setThumbnailUrl(googleThumbnailUrl);
                            Log.d(TAG, "Google Books API: Using Image for '" + title + "': " + googleThumbnailUrl);
                        } else {
                            Log.d(TAG, "Google Books API: No suitable image found for '" + title + "'. Keeping Rakuten image if any.");
                        }
                        Log.d(TAG, "Google Books API: Successfully updated Book ID to " + googleId + " for Title: " + title);
                    } else {
                        Log.d(TAG, "Google Books API: No items found for query: " + googleQuery + " (Title: " + title + ")");
                    }
                } else {
                    Log.e(TAG, "Google Books API call failed for '" + title + "': " + googleResponse.code() + " " + googleResponse.message());
                }
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "URLエンコードエラー (Google二次検索): " + e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, "Google Books API通信エラー (二次検索): " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "予期せぬエラー (Google二次検索): " + e.getMessage(), e);
        }
    }

    public void shutdown() {
        executorService.shutdown();
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