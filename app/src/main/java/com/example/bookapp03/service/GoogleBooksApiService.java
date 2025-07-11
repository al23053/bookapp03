package com.example.bookapp03.service;

import android.util.Log;

import com.example.bookapp03.model.Book;
import com.example.bookapp03.model.BooksApiResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Google Books APIにアクセスするためのサービスです。
 * 書籍の検索候補の取得と書籍情報の検索ロジックを担当します。
 */
public class GoogleBooksApiService {

    private static final String TAG = "GoogleBooksApiService";
    private static final String API_BASE_URL = "https://www.googleapis.com/books/v1/";
    private static final String API_KEY = "AIzaSyA-s5GGsog_X0cSOWUz4CuMtZ7M5ug7lTI";
    private final OkHttpClient okHttpClient;
    private final Gson gson;
    private final ExecutorService executorService;

    /**
     * API_KEY が設定されていることを確認するヘルパーメソッドです。
     * 開発中にAPIキーの未設定を防ぐための警告を出力します。
     */
    private void checkApiKey() {
        if (API_KEY.equals("YOUR_API_KEY") || API_KEY.isEmpty()) {
            Log.e(TAG, "警告: Google Books API キーが設定されていません。YOUR_API_KEY を実際のキーに置き換えてください。");
        }
    }

    /**
     * GoogleBooksApiServiceのコンストラクタです。
     * OkHttpClient、Gson、および非同期処理用のExecutorServiceを初期化します。
     */
    public GoogleBooksApiService() {
        // OkHttpClient を初期化 (タイムアウトを設定)
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS) // 接続タイムアウト
                .writeTimeout(10, TimeUnit.SECONDS)    // 書き込みタイムアウト
                .readTimeout(30, TimeUnit.SECONDS)     // 読み込みタイムアウト
                .build();
        this.gson = new Gson();
        this.executorService = Executors.newFixedThreadPool(4);
        checkApiKey(); // APIキーのチェック
    }

    /**
     * 検索候補取得結果を通知するためのコールバックインターフェースです。
     */
    public interface SuggestionCallback {
        /**
         * 検索候補が正常に受信されたときに呼び出されます。
         *
         * @param suggestions 検索候補の文字列リスト
         */
        void onSuggestionsReceived(List<String> suggestions);

        /**
         * 検索候補の取得中にエラーが発生したときに呼び出されます。
         *
         * @param errorMessage エラーメッセージ
         */
        void onFailure(String errorMessage);
    }

    /**
     * Google Books APIから検索候補（サジェスト）を取得します。
     * この処理はバックグラウンドスレッドで実行されます。
     *
     * @param query    検索クエリ
     * @param callback 結果を返すコールバック
     */
    public void fetchSuggestions(String query, SuggestionCallback callback) {
        if (query == null || query.trim().isEmpty()) {
            callback.onSuggestionsReceived(Collections.emptyList());
            return;
        }

        // ExecutorService を使用して非同期でAPI呼び出しを実行
        executorService.execute(() -> {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(API_BASE_URL + "volumes").newBuilder();
            urlBuilder.addQueryParameter("q", query);
            urlBuilder.addQueryParameter("maxResults", "5"); // 取得する候補の数
            urlBuilder.addQueryParameter("key", API_KEY);

            String url = urlBuilder.build().toString();
            Request request = new Request.Builder().url(url).build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    BooksApiResponse apiResponse = gson.fromJson(json, BooksApiResponse.class);
                    List<String> suggestions = new ArrayList<>();
                    if (apiResponse != null && apiResponse.getItems() != null) {
                        for (BooksApiResponse.Item item : apiResponse.getItems()) {
                            if (item.getVolumeInfo() != null && item.getVolumeInfo().getTitle() != null) {
                                suggestions.add(item.getVolumeInfo().getTitle());
                            }
                        }
                    }
                    callback.onSuggestionsReceived(suggestions);
                } else {
                    String errorMessage = "APIエラー: " + response.code() + " " + response.message();
                    Log.e(TAG, errorMessage);
                    callback.onFailure(errorMessage);
                }
            } catch (IOException e) {
                Log.e(TAG, "ネットワークエラー: " + e.getMessage(), e);
                callback.onFailure("ネットワークエラー: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "JSONパースエラーまたはその他のエラー: " + e.getMessage(), e);
                callback.onFailure("データ処理エラー: " + e.getMessage());
            }
        });
    }

    /**
     * 書籍検索結果を通知するためのコールバックインターフェースです。
     */
    public interface SearchCallback {
        /**
         * 書籍検索結果が正常に受信されたときに呼び出されます。
         *
         * @param books 検索結果の書籍リスト
         */
        void onSearchResultsReceived(List<Book> books);

        /**
         * 書籍検索中にエラーが発生したときに呼び出されます。
         *
         * @param errorMessage エラーメッセージ
         */
        void onFailure(String errorMessage);
    }

    /**
     * Google Books APIから書籍を検索します。
     * この処理はバックグラウンドスレッドで実行されます。
     *
     * @param query    検索クエリ
     * @param callback 結果を返すコールバック
     */
    public void searchBooks(String query, SearchCallback callback) {
        if (query == null || query.trim().isEmpty()) {
            callback.onSearchResultsReceived(Collections.emptyList());
            return;
        }

        executorService.execute(() -> {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(API_BASE_URL + "volumes").newBuilder();
            urlBuilder.addQueryParameter("q", query);
            urlBuilder.addQueryParameter("maxResults", "40"); // より多くの結果を取得
            urlBuilder.addQueryParameter("key", API_KEY);

            String url = urlBuilder.build().toString();
            Request request = new Request.Builder().url(url).build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    BooksApiResponse apiResponse = gson.fromJson(json, BooksApiResponse.class);
                    List<Book> books = new ArrayList<>();
                    if (apiResponse != null && apiResponse.getItems() != null) {
                        for (BooksApiResponse.Item item : apiResponse.getItems()) {
                            // APIレスポンスのItemからBookモデルに変換
                            Book book = convertApiItemToBook(item);
                            if (book != null) {
                                books.add(book);
                            }
                        }
                    }
                    callback.onSearchResultsReceived(books);
                } else {
                    String errorMessage = "APIエラー: " + response.code() + " " + response.message();
                    Log.e(TAG, errorMessage);
                    callback.onFailure(errorMessage);
                }
            } catch (IOException e) {
                Log.e(TAG, "ネットワークエラー: " + e.getMessage(), e);
                callback.onFailure("ネットワークエラー: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "JSONパースエラーまたはその他のエラー: " + e.getMessage(), e);
                callback.onFailure("データ処理エラー: " + e.getMessage());
            }
        });
    }

    /**
     * Google Books APIのItemオブジェクトを、アプリケーションのBookモデルに変換するヘルパーメソッドです。
     *
     * @param item Google Books APIのレスポンスから得られたItemオブジェクト
     * @return 変換されたBookオブジェクト、またはitemやvolumeInfoがnullの場合はnull
     */
    private Book convertApiItemToBook(BooksApiResponse.Item item) {
        if (item == null || item.getVolumeInfo() == null) {
            return null;
        }

        BooksApiResponse.VolumeInfo volumeInfo = item.getVolumeInfo();
        String id = item.getId();
        String title = volumeInfo.getTitle();
        String author = (volumeInfo.getAuthors() != null && !volumeInfo.getAuthors().isEmpty())
                ? volumeInfo.getAuthors().get(0)
                : "著者不明";
        String description = volumeInfo.getDescription();
        String thumbnailUrl = (volumeInfo.getImageLinks() != null)
                ? volumeInfo.getImageLinks().getThumbnail()
                : null;
        List<String> categories = volumeInfo.getCategories();
        Book book = new Book(id, title, author, description, thumbnailUrl, categories);
        book.setPublishedDate(volumeInfo.getPublishedDate());
        return book;
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
                executorService.shutdownNow(); // タイムアウト後も終了しない場合、即時シャットダウン
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow(); // シャットダウン中に割り込まれた場合も即時シャットダウン
            Thread.currentThread().interrupt(); // 現在のスレッドの割り込み状態を再設定
        }
        Log.d(TAG, "GoogleBooksApiService ExecutorService shut down.");
    }
}
