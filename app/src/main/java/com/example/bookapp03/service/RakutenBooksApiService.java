package com.example.bookapp03.service;

import android.util.Log;

import com.example.bookapp03.model.Book;
import com.example.bookapp03.model.BooksApiResponse; // このクラスは使われていないようですが、残しておきます
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
import java.util.stream.Collectors; // Stream APIのために追加


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
    // 楽天市場ランキングAPIのエンドポイントに変更
    private static final String RAKUTEN_ICHIBA_RANKING_API_URL = "https://app.rakuten.co.jp/services/api/IchibaItem/Ranking/20170628";
    private static final String GOOGLE_BOOKS_API_BASE_URL = "https://www.googleapis.com/books/v1/volumes";

    /**
     * 楽天市場APIにアクセスするためのアプリケーションID。
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
     * 楽天市場ランキングAPIから売れ筋の書籍（ランキング）を取得し、可能であればGoogle Books APIで情報を補完します。
     * この処理はバックグラウンドスレッドで実行されます。
     *
     * @param callback 結果を返すRakutenBooksApiCallback
     */
    public void fetchRankingBooks(RakutenBooksApiCallback callback) {
        executorService.execute(() -> {
            try {
                // 楽天市場ランキングAPIのリクエストURLを構築
                HttpUrl.Builder urlBuilder = HttpUrl.parse(RAKUTEN_ICHIBA_RANKING_API_URL).newBuilder();
                urlBuilder.addQueryParameter("applicationId", rakutenApplicationId);
                urlBuilder.addQueryParameter("genreId", "200162"); // 楽天市場の「本・雑誌・コミック」ジャンルIDの例
                urlBuilder.addQueryParameter("hits", "10"); // 取得する書籍数を10件に制限
                urlBuilder.addQueryParameter("elements", "itemName,artistName,itemUrl,mediumImageUrl,affiliateUrl"); // 取得する要素を指定 (楽天市場ランキングAPIの要素名)


                String url = urlBuilder.build().toString();
                Log.d(TAG, "Rakuten Ranking API URL: " + url);
                Request request = new Request.Builder().url(url).build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("楽天市場ランキングAPI呼び出し失敗: " + response.code() + " " + response.message());
                    }

                    String responseBody = response.body().string();
                    Log.d(TAG, "Rakuten Ichiba Ranking API Response: " + responseBody);

                    // JSONレスポンスをパース
                    JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                    JsonArray itemsArray = jsonObject.getAsJsonArray("Items"); // "Items"キーの下に配列がある

                    List<Book> hotBooks = new ArrayList<>();
                    if (itemsArray != null) {
                        for (JsonElement itemElement : itemsArray) {
                            JsonObject itemObject = itemElement.getAsJsonObject().getAsJsonObject("Item"); // 各要素が "Item" オブジェクトの中にネストされている
                            if (itemObject != null) {
                                // 楽天市場ランキングAPIから書籍情報を抽出 (要素名に注意)
                                String rakutenTitle = itemObject.has("itemName") ? itemObject.get("itemName").getAsString() : "タイトル不明";
                                String rakutenAuthor = itemObject.has("artistName") ? itemObject.get("artistName").getAsString() : "著者不明"; // artistName が著者名として使われることが多い
                                String rakutenItemUrl = itemObject.has("itemUrl") ? itemObject.get("itemUrl").getAsString() : null;
                                // 楽天市場ランキングAPIでは largeImageUrl ではなく mediumImageUrl が提供されることが多い
                                String rakutenMediumImageUrl = itemObject.has("mediumImageUrl") ? itemObject.get("mediumImageUrl").getAsString() : null;
                                String rakutenIsbn = itemObject.has("isbn") ? itemObject.get("isbn").getAsString() : null; // ★ISBN取得


                                Book book = new Book();
                                book.setTitle(rakutenTitle);
                                book.setAuthor(rakutenAuthor);
                                book.setRakutenItemUrl(rakutenItemUrl);
                                book.setRakutenLargeImageUrl(rakutenMediumImageUrl);
                                book.setThumbnailUrl(rakutenMediumImageUrl); // 最初は楽天の画像URLを設定
                                book.setIsbn(rakutenIsbn); // ★ISBNをBookオブジェクトにセット

                                Log.d(TAG, "Processing Rakuten Book: Title=" + rakutenTitle + ", Author=" + rakutenAuthor + ", RakutenImage=" + rakutenMediumImageUrl + ", ISBN=" + rakutenIsbn); // ★追加ログ
                                fetchGoogleBooksDataForRakutenBook(book, rakutenTitle, rakutenAuthor,rakutenIsbn);

                                hotBooks.add(book);
                            }
                        }
                    }
                    callback.onSuccess(hotBooks);
                }
            } catch (IOException e) {
                // ネットワーク関連のエラーハンドリング
                Log.e(TAG, "楽天市場ランキングAPI呼び出しエラー: " + e.getMessage(), e);
                callback.onFailure("話題の本の取得に失敗しました: " + e.getMessage());
            } catch (Exception e) {
                // JSONパースなど、その他のエラーハンドリング
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
     */
    private void fetchGoogleBooksDataForRakutenBook(Book book, String title, String author, String isbn) {
        try {
            String googleQuery;
            if (isbn != null && !isbn.isEmpty() && !isbn.equals("null")) { // ISBNが有効な値かチェック
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
            Log.d(TAG, "Google Books API URL: " + googleBooksApiUrl); // ★追加ログ
            Request googleRequest = new Request.Builder().url(googleBooksApiUrl).build();

            try (Response googleResponse = httpClient.newCall(googleRequest).execute()) {
                if (googleResponse.isSuccessful() && googleResponse.body() != null) {
                    String googleResponseBody = googleResponse.body().string();
                    Log.d(TAG, "Google Books API Raw Response for '" + title + "': " + googleResponseBody); // ★追加ログ

                    JsonObject googleJson = JsonParser.parseString(googleResponseBody).getAsJsonObject();
                    JsonArray googleItems = googleJson.getAsJsonArray("items");

                    if (googleItems != null && googleItems.size() > 0) {
                        JsonObject firstItem = googleItems.get(0).getAsJsonObject();
                        JsonObject volumeInfo = firstItem.getAsJsonObject("volumeInfo");

                        String googleId = firstItem.has("id") ? firstItem.get("id").getAsString() : null;
                        String infoLink = volumeInfo.has("infoLink") ? volumeInfo.get("infoLink").getAsString() : null;
                        String googleThumbnailUrl = null; // Google Books APIから取得する画像URLを区別するため変数名を変更
                        if (volumeInfo.has("imageLinks")) {
                            JsonObject imageLinks = volumeInfo.getAsJsonObject("imageLinks");
                            // より高解像度の画像があればそちらを優先する
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

                        book.setId(googleId);
                        book.setInfoLink(infoLink);
                        // Google Books APIから画像が取得できた場合のみ、楽天の画像を上書きする
                        if (googleThumbnailUrl != null) {
                            book.setThumbnailUrl(googleThumbnailUrl);
                            Log.d(TAG, "Google Books API: Using Image for '" + title + "': " + googleThumbnailUrl);
                        } else {
                            Log.d(TAG, "Google Books API: No suitable image found for '" + title + "'. Keeping Rakuten image if any.");
                        }
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