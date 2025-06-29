package com.example.bookapp03.service;

import android.util.Log;

import com.example.bookapp03.model.Book;
import com.example.bookapp03.model.BooksApiResponse;
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
 * 楽天Kobo APIにアクセスするためのサービスです。
 * 書籍ランキングの取得と、その結果をGoogle Books APIで補完するロジックを担当します。
 */
public class RakutenBooksApiService {

    private static final String TAG = "RakutenBooksApiService";
    private static final String RAKUTEN_BOOKS_API_RANKING_URL = "https://app.rakuten.co.jp/services/api/Kobo/BookSearch/20170424";
    private static final String GOOGLE_BOOKS_API_BASE_URL = "https://www.googleapis.com/books/v1/volumes";

    /**
     * 楽天APIにアクセスするためのアプリケーションID。
     */
    private final String rakutenApplicationId;
    /**
     * 楽天APIで取得した書籍情報を補完するために使用するGoogle Books APIのキー。
     */
    private final String googleBooksApiKey;
    /**
     * HTTPリクエストを実行するためのOkHttpClientインスタンス。
     */
    private final OkHttpClient httpClient;
    /**
     * JSONデータのシリアライズ/デシリアライズを行うためのGsonインスタンス。
     */
    private final Gson gson;
    /**
     * 非同期処理を実行するためのExecutorService。
     */
    private final ExecutorService executorService;

    /**
     * RakutenBooksApiServiceのコンストラクタです。
     * HTTPクライアント、Gson、および各APIキーを注入してサービスを初期化します。
     *
     * @param httpClient           共有のOkHttpClientインスタンス
     * @param gson                 共有のGsonインスタンス
     * @param rakutenApplicationId 楽天APIのアプリケーションID
     * @param googleBooksApiKey    楽天サービス内のGoogle Books API二次検索用キー
     */
    public RakutenBooksApiService(OkHttpClient httpClient, Gson gson, String rakutenApplicationId, String googleBooksApiKey) {
        this.httpClient = httpClient;
        this.gson = gson;
        this.rakutenApplicationId = rakutenApplicationId;
        this.googleBooksApiKey = googleBooksApiKey;
        this.executorService = Executors.newFixedThreadPool(2);
    }

    /**
     * 楽天ブックスAPIからのランキング取得結果を通知するためのコールバックインターフェースです。
     */
    public interface RakutenBooksApiCallback {
        /**
         * 話題の書籍リストが正常に受信されたときに呼び出されます。
         *
         * @param hotBooks 話題の書籍（ランキング）のリスト
         */
        void onSuccess(List<Book> hotBooks);

        /**
         * 話題の書籍の取得中にエラーが発生したときに呼び出されます。
         *
         * @param errorMessage エラーメッセージ
         */
        void onFailure(String errorMessage);
    }


    /**
     * 楽天Kobo APIから話題の書籍（ランキング）を取得し、可能であればGoogle Books APIで情報を補完します。
     * この処理はバックグラウンドスレッドで実行されます。
     *
     * @param callback 結果を返すRakutenBooksApiCallback
     */
    public void fetchRankingBooks(RakutenBooksApiCallback callback) {
        executorService.execute(() -> {
            try {
                // 楽天APIのリクエストURLを構築
                HttpUrl.Builder urlBuilder = HttpUrl.parse(RAKUTEN_BOOKS_API_RANKING_URL).newBuilder();
                urlBuilder.addQueryParameter("applicationId", rakutenApplicationId);
                urlBuilder.addQueryParameter("booksGenreId", "001"); // 全体ランキングのジャンルID
                urlBuilder.addQueryParameter("formatVersion", "2"); // APIのフォーマットバージョン
                urlBuilder.addQueryParameter("elements", "title,author,itemUrl,largeImageUrl"); // 取得する要素を指定
                urlBuilder.addQueryParameter("hits", "10"); // 取得する書籍数を10件に制限

                String url = urlBuilder.build().toString();
                Request request = new Request.Builder().url(url).build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("楽天API呼び出し失敗: " + response.code() + " " + response.message());
                    }

                    String responseBody = response.body().string();
                    Log.d(TAG, "Rakuten Ranking API Response: " + responseBody);

                    // JSONレスポンスをパース
                    JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                    JsonArray itemsArray = jsonObject.getAsJsonArray("Items");

                    List<Book> hotBooks = new ArrayList<>();
                    if (itemsArray != null) {
                        for (JsonElement itemElement : itemsArray) {
                            JsonObject itemObject = itemElement.getAsJsonObject().getAsJsonObject("Item");
                            if (itemObject != null) {
                                // 楽天APIから書籍情報を抽出
                                String rakutenTitle = itemObject.has("title") ? itemObject.get("title").getAsString() : "タイトル不明";
                                String rakutenAuthor = itemObject.has("author") ? itemObject.get("author").getAsString() : "著者不明";
                                String rakutenItemUrl = itemObject.has("itemUrl") ? itemObject.get("itemUrl").getAsString() : null;
                                String rakutenLargeImageUrl = itemObject.has("largeImageUrl") ? itemObject.get("largeImageUrl").getAsString() : null;

                                Book book = new Book();
                                book.setTitle(rakutenTitle);
                                book.setAuthor(rakutenAuthor);
                                book.setRakutenItemUrl(rakutenItemUrl);
                                book.setRakutenLargeImageUrl(rakutenLargeImageUrl);
                                book.setThumbnailUrl(rakutenLargeImageUrl);
                                fetchGoogleBooksDataForRakutenBook(book, rakutenTitle, rakutenAuthor);

                                hotBooks.add(book);
                            }
                        }
                    }
                    callback.onSuccess(hotBooks);
                }
            } catch (IOException e) {
                // ネットワーク関連のエラーハンドリング
                Log.e(TAG, "楽天ランキングAPI呼び出しエラー: " + e.getMessage(), e);
                callback.onFailure("話題の本の取得に失敗しました: " + e.getMessage());
            } catch (Exception e) {
                // JSONパースなど、その他のエラーハンドリング
                Log.e(TAG, "楽天ランキングデータ処理エラー: " + e.getMessage(), e);
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
     */
    private void fetchGoogleBooksDataForRakutenBook(Book book, String title, String author) {
        try {
            String googleQuery = URLEncoder.encode(title + " " + author, "UTF-8");
            HttpUrl.Builder googleUrlBuilder = HttpUrl.parse(GOOGLE_BOOKS_API_BASE_URL).newBuilder();
            googleUrlBuilder.addQueryParameter("q", googleQuery);
            googleUrlBuilder.addQueryParameter("key", googleBooksApiKey);
            googleUrlBuilder.addQueryParameter("maxResults", "1");

            String googleBooksApiUrl = googleUrlBuilder.build().toString();
            Request googleRequest = new Request.Builder().url(googleBooksApiUrl).build();

            try (Response googleResponse = httpClient.newCall(googleRequest).execute()) {
                if (googleResponse.isSuccessful() && googleResponse.body() != null) {
                    String googleResponseBody = googleResponse.body().string();
                    // Google Books APIのJSONレスポンスをパース
                    JsonObject googleJson = JsonParser.parseString(googleResponseBody).getAsJsonObject();
                    JsonArray googleItems = googleJson.getAsJsonArray("items");

                    if (googleItems != null && googleItems.size() > 0) {
                        JsonObject firstItem = googleItems.get(0).getAsJsonObject();
                        JsonObject volumeInfo = firstItem.getAsJsonObject("volumeInfo");

                        // Google Books APIから詳細情報を抽出
                        String googleId = firstItem.has("id") ? firstItem.get("id").getAsString() : null;
                        String infoLink = volumeInfo.has("infoLink") ? volumeInfo.get("infoLink").getAsString() : null;
                        String thumbnailUrl = null;
                        if (volumeInfo.has("imageLinks")) {
                            JsonObject imageLinks = volumeInfo.getAsJsonObject("imageLinks");
                            if (imageLinks.has("thumbnail")) {
                                thumbnailUrl = imageLinks.get("thumbnail").getAsString();
                            }
                        }

                        // BookオブジェクトにGoogle Books APIの情報をセット
                        book.setId(googleId);
                        book.setInfoLink(infoLink);
                        if (thumbnailUrl != null) {
                            book.setThumbnailUrl(thumbnailUrl);
                        }
                        Log.d(TAG, "Google Books API found for " + title + ": ID=" + googleId);
                    } else {
                        Log.d(TAG, "Google Books API not found for: " + title);
                    }
                } else {
                    Log.e(TAG, "Google Books API call failed for " + title + ": " + googleResponse.code() + " " + googleResponse.message());
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

    /**
     * このサービスで使用されているExecutorServiceをシャットダウンします。
     * アプリケーションが終了する際に呼び出す必要があります。
     * これにより、バックグラウンドスレッドが適切に終了し、リソースリークを防ぎます。
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow(); // タイムアウト後も終了しない場合、即時シャットダウンを試みる
            }
        } catch (InterruptedException e) {
            // 待機中に現在のスレッドが割り込まれた場合
            executorService.shutdownNow(); // 即時シャットダウンを試みる
            Thread.currentThread().interrupt(); // 現在のスレッドの割り込み状態を再設定（ベストプラクティス）
        }
        Log.d(TAG, "RakutenBooksApiService ExecutorService shut down.");
    }
}
