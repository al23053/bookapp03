package com.example.bookapp03.manager;

import android.util.Log;

import com.example.bookapp03.model.Book;
import com.example.bookapp03.service.FirestoreBookService;
import com.example.bookapp03.service.GoogleBooksApiService;
import com.example.bookapp03.service.RakutenBooksApiService;
import com.example.bookapp03.service.UserGenreService;
import com.google.gson.Gson;

import java.util.List;

import okhttp3.OkHttpClient;

/**
 * アプリケーション全体の書籍データおよびユーザーデータアクセスを管理する層。
 * 各サービス（Google Books API, Rakuten Books API, Firestore）へのアクセスを仲介します。
 */
public class BookAppManager {

    private static final String TAG = "BookAppManager";

    // 依存する各サービス
    private final GoogleBooksApiService googleBooksApiService;
    private final RakutenBooksApiService rakutenBooksApiService;
    private final FirestoreBookService firestoreBookService;
    private final UserGenreService userGenreService;

    public interface SuggestionsCallback {
        void onSuggestionsReceived(List<String> suggestions);

        void onFailure(String errorMessage);
    }

    public interface SearchCallback {
        void onSearchResultsReceived(List<Book> books);

        void onFailure(String errorMessage);
    }

    public interface HotBooksCallback {
        void onHotBooksReceived(List<Book> hotBooks);

        void onFailure(String errorMessage);
    }

    public interface RecommendedBooksCallback {
        void onRecommendationsReceived(List<Book> matchingBooks, List<Book> nonMatchingBooks);

        void onFailure(String errorMessage);
    }

    public interface UserGenresCallback {
        void onGenresReceived(List<String> genres);

        void onFailure(String errorMessage);
    }


    /**
     * BookAppManagerのコンストラクタ。
     * 必要な依存関係（OkHttpClient, Gson, APIキー）を受け取り、各サービスを初期化します。
     *
     * @param httpClient                                 共有のOkHttpClientインスタンス
     * @param gson                                       共有のGsonインスタンス
     * @param rakutenApplicationId                       楽天APIのアプリケーションID
     * @param googleBooksApiKeyForRakutenSecondarySearch 楽天サービス内のGoogle Books API二次検索用キー
     */
    public BookAppManager(OkHttpClient httpClient, Gson gson,
                          String rakutenApplicationId, String googleBooksApiKeyForRakutenSecondarySearch) {
        // GoogleBooksApiService は内部で OkHttpClient と Gson を初期化するため、引数なしのコンストラクタを使用
        this.googleBooksApiService = new GoogleBooksApiService();
        this.rakutenBooksApiService = new RakutenBooksApiService(httpClient, gson, rakutenApplicationId, googleBooksApiKeyForRakutenSecondarySearch);
        this.firestoreBookService = new FirestoreBookService(httpClient, gson, googleBooksApiKeyForRakutenSecondarySearch); // rakutenSecondarySearch 用のキーを共有
        this.userGenreService = new UserGenreService();
        Log.d(TAG, "BookAppManager initialized.");
    }

    /**
     * Google Books APIから検索候補を取得します。
     *
     * @param query    検索クエリ
     * @param callback 結果を返すコールバック
     */
    public void fetchSuggestions(String query, final SuggestionsCallback callback) {
        googleBooksApiService.fetchSuggestions(query, new GoogleBooksApiService.SuggestionCallback() {
            @Override
            public void onSuggestionsReceived(List<String> suggestions) {
                callback.onSuggestionsReceived(suggestions);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    /**
     * Google Books APIから書籍を検索します。
     *
     * @param query    検索クエリ
     * @param callback 結果を返すコールバック
     */
    public void searchBooks(String query, final SearchCallback callback) {
        googleBooksApiService.searchBooks(query, new GoogleBooksApiService.SearchCallback() {
            @Override
            public void onSearchResultsReceived(List<Book> books) {
                callback.onSearchResultsReceived(books);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    /**
     * 楽天市場 APIから話題の書籍（ランキング）を取得します。
     *
     * @param callback 結果を返すコールバック
     */
    public void fetchHotBooks(final HotBooksCallback callback) {
        rakutenBooksApiService.fetchRankingBooks(new RakutenBooksApiService.RakutenBooksApiCallback() {
            @Override
            public void onSuccess(List<Book> hotBooks) {
                callback.onHotBooksReceived(hotBooks);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    /**
     * Firestoreからユーザーの好きなジャンルを取得します。
     *
     * @param userId   ユーザーID
     * @param callback 結果を返すコールバック
     */
    public void fetchUserFavoriteGenres(String userId, final UserGenresCallback callback) {
        userGenreService.getUserFavoriteGenres(userId, new UserGenreService.UserGenresCallback() {
            @Override
            public void onGenresReceived(List<String> genres) {
                callback.onGenresReceived(genres);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    /**
     * Firestoreからおすすめの書籍（ユーザーの好きなジャンルに合う・合わない）を取得します。
     *
     * @param favoriteGenres ユーザーが好きなジャンルのリスト
     * @param callback       結果を通知するコールバック
     */
    public void getRecommendedBooksFromFirestore(List<String> favoriteGenres, final RecommendedBooksCallback callback) {
        firestoreBookService.getRecommendedBooksFromFirestore(favoriteGenres, new FirestoreBookService.BookRecommendationCallback() {
            @Override
            public void onRecommendationsReceived(List<Book> matchingBooks, List<Book> nonMatchingBooks) {
                callback.onRecommendationsReceived(matchingBooks, nonMatchingBooks);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    /**
     * BookAppManagerが管理するサービスのリソースをシャットダウンします。
     * アプリケーションが終了する際に呼び出す必要があります。
     */
    public void shutdown() {
        if (googleBooksApiService != null) {
            googleBooksApiService.shutdown();
        }
        if (rakutenBooksApiService != null) {
            rakutenBooksApiService.shutdown();
        }
        Log.d(TAG, "BookAppManager services shut down.");
    }
}