package com.example.bookapp03.Serchmain;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp03.R;
import com.example.bookapp03.adapter.BookCardAdapter;
import com.example.bookapp03.model.Book;
import com.example.bookapp03.ui.UserReviewListActivity;

// 管理部、各種処理部、カスタムビューをインポート
import com.example.bookapp03.manager.BookAppManager;
import com.example.bookapp03.processor.SearchFeatureProcessor;
import com.example.bookapp03.processor.UserFeatureProcessor;
import com.example.bookapp03.processor.BookFeatureProcessor;
import com.example.bookapp03.view.SearchInputView;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * アプリケーションのメインアクティビティです。
 * ユーザーインターフェースの表示、ユーザー操作のハンドリング、
 * そして各処理部（検索、ユーザー情報、本の情報）へのリクエストの委譲を担当します。
 * BookCardAdapter.OnBookClickListenerを実装し、書籍カードのクリックイベントを処理します。
 */
public class MainActivity extends AppCompatActivity implements BookCardAdapter.OnBookClickListener {

    /**
     * 楽天サービス内でGoogle Books APIの二次検索に使用されるAPIキーです。
     * 本番環境ではBuildConfigなどでの管理が推奨されます。
     */
    private static final String GOOGLE_BOOKS_API_KEY_FOR_RAKUTEN_SECONDARY_SEARCH = "AIzaSyBgulCNwWRSj95lHveqv67KfC39RMNINyM";

    /**
     * 楽天Kobo APIにアクセスするためのアプリケーションIDです。
     * ★実際の楽天アプリケーションIDに置き換える必要があります。
     */
    private static final String RAKUTEN_APPLICATION_ID = "1063453786270383298";

    /**
     * 検索入力、検索ボタン、検索候補リストを含むカスタムビュー。
     */
    private SearchInputView searchInputView;

    /**
     * ユーザーの好きなジャンルに一致する書籍を表示するRecyclerView。
     */
    private RecyclerView matchingBooksRecyclerView;
    /**
     * ユーザーの好きなジャンルに一致しない書籍を表示するRecyclerView。
     */
    private RecyclerView nonMatchingBooksRecyclerView;
    /**
     * ユーザーの好きなジャンルに一致する書籍リストのアダプター。
     */
    private BookCardAdapter matchingBooksAdapter;
    /**
     * ユーザーの好きなジャンルに一致しない書籍リストのアダプター。
     */
    private BookCardAdapter nonMatchingBooksAdapter;
    /**
     * 「あなたに合う本」セクションのヘッダーテキストビュー。
     */
    private TextView matchingBooksHeader;
    /**
     * 「いつもは読まない本」セクションのヘッダーテキストビュー。
     */
    private TextView nonMatchingBooksHeader;
    /**
     * おすすめ書籍セクション全体を囲むレイアウトコンテナ。
     */
    private LinearLayout recommendedBooksContainer;
    /**
     * 「あなたに合う本」がない場合に表示されるメッセージ。
     */
    private TextView noMatchingBooksMessage;
    /**
     * 「いつもは読まない本」がない場合に表示されるメッセージ。
     */
    private TextView noNonMatchingBooksMessage;
    /**
     * 話題の書籍を表示するRecyclerView。
     */
    private RecyclerView hotBooksRecyclerView;
    /**
     * 話題の書籍リストのアダプター。
     */
    private BookCardAdapter hotBooksAdapter;
    /**
     * 「話題の本」セクションのヘッダーテキストビュー。
     */
    private TextView hotBooksHeader;
    /**
     * 「話題の本」がない場合に表示されるメッセージ。
     */
    private TextView noHotBooksMessage;

    /**
     * HTTPリクエストを実行するためのOkHttpClientインスタンス。
     * 管理部（BookAppManager）に依存性注入されます。
     */
    private OkHttpClient httpClient;
    /**
     * JSONデータのシリアライズ/デシリアライズを行うためのGsonインスタンス。
     * 管理部（BookAppManager）に依存性注入されます。
     */
    private Gson gson;

    /**
     * UIスレッドでUIの更新を行うためのHandler。
     */
    private Handler mainHandler;

    // 管理部インスタンス
    /**
     * アプリケーション全体のデータアクセスを調整・管理する管理部インスタンス。
     */
    private BookAppManager bookAppManager;
    // 各種処理部インスタンス
    /**
     * 検索機能に関するビジネスロジックを処理する検索処理部インスタンス。
     */
    private SearchFeatureProcessor searchFeatureProcessor;
    /**
     * ユーザー情報に関するビジネスロジックを処理するユーザー情報処理部インスタンス。
     */
    private UserFeatureProcessor userFeatureProcessor;
    /**
     * 本の情報に関するビジネスロジックを処理する本の情報処理部インスタンス。
     */
    private BookFeatureProcessor bookFeatureProcessor;


    /**
     * Activityが最初に作成されるときに呼び出されます。
     * レイアウトの設定、UI要素の初期化、サービス/マネージャーの初期化、
     * そして初期データ（ユーザーの好み、話題の本）の取得を行います。
     *
     * @param savedInstanceState 以前に保存された状態データを含むBundleオブジェクト。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_main);
        searchInputView = findViewById(R.id.main_search_input_view);
        matchingBooksRecyclerView = findViewById(R.id.matching_books_recycler_view);
        nonMatchingBooksRecyclerView = findViewById(R.id.non_matching_books_recycler_view);
        matchingBooksHeader = findViewById(R.id.matching_books_header);
        nonMatchingBooksHeader = findViewById(R.id.non_matching_books_header);
        recommendedBooksContainer = findViewById(R.id.recommended_books_container);
        noMatchingBooksMessage = findViewById(R.id.no_matching_books_message);
        noNonMatchingBooksMessage = findViewById(R.id.no_non_matching_books_message);

        hotBooksRecyclerView = findViewById(R.id.hot_books_recycler_view);
        hotBooksHeader = findViewById(R.id.hot_books_header);
        noHotBooksMessage = findViewById(R.id.no_hot_books_message);

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS) // 接続タイムアウト
                .writeTimeout(10, TimeUnit.SECONDS)    // 書き込みタイムアウト
                .readTimeout(30, TimeUnit.SECONDS)     // 読み込みタイムアウト
                .build();
        gson = new Gson();

        mainHandler = new Handler(Looper.getMainLooper());

        // 管理部を初期化し、必要な依存関係を注入 (DI)
        bookAppManager = new BookAppManager(httpClient, gson, RAKUTEN_APPLICATION_ID, GOOGLE_BOOKS_API_KEY_FOR_RAKUTEN_SECONDARY_SEARCH);
        // 各種処理部を初期化し、管理部を注入
        searchFeatureProcessor = new SearchFeatureProcessor(bookAppManager);
        userFeatureProcessor = new UserFeatureProcessor(bookAppManager);
        bookFeatureProcessor = new BookFeatureProcessor(bookAppManager);

        searchInputView.setSearchFeatureProcessor(searchFeatureProcessor);
        searchInputView.setOnSearchActionListener(new SearchInputView.OnSearchActionListener() {
            /**
             * 検索が正常に実行され、結果が返されたときに呼び出されます。
             * 検索結果画面へ遷移します。
             * @param searchResults 検索結果の書籍リスト
             */
            @Override
            public void onSearchPerformed(List<Book> searchResults) {
                mainHandler.post(() -> {
                    if (!searchResults.isEmpty()) {
                        String jsonSearchResults = gson.toJson(searchResults);
                        Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
                        intent.putExtra(SearchResultActivity.EXTRA_SEARCH_RESULTS, jsonSearchResults);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "検索結果が見つかりませんでした。", Toast.LENGTH_LONG).show();
                    }
                });
            }

            /**
             * 検索の実行中にエラーが発生したときに呼び出されます。
             * @param errorMessage エラーメッセージ
             */
            @Override
            public void onSearchFailed(String errorMessage) {
                mainHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "書籍検索に失敗しました: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });

        setupRecommendedBooksRecyclerViews();

        String currentUserId = "testUser1";

        fetchUserFavoriteGenres(currentUserId);

        fetchRakutenRankingBooks();
    }

    /**
     * Activityが破棄されるときに呼び出されます。
     * 各処理部や管理部が使用しているリソースを適切にシャットダウンします。
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bookAppManager != null) {
            bookAppManager.shutdown();
        }
        if (searchInputView != null) {
            searchInputView.onDestroy();
        }
    }


    /**
     * おすすめ書籍（ユーザーの好みに合う本、合わない本、話題の本）を表示する
     * RecyclerViewsの初期設定を行います。
     */
    private void setupRecommendedBooksRecyclerViews() {
        // 合う本のRecyclerViewの初期設定
        matchingBooksRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        matchingBooksAdapter = new BookCardAdapter(new ArrayList<>(), this);
        matchingBooksRecyclerView.setAdapter(matchingBooksAdapter);

        // 合わない本のRecyclerViewの初期設定
        nonMatchingBooksRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        nonMatchingBooksAdapter = new BookCardAdapter(new ArrayList<>(), this);
        nonMatchingBooksRecyclerView.setAdapter(nonMatchingBooksAdapter);

        // 話題の本のRecyclerViewの初期設定
        hotBooksRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        hotBooksAdapter = new BookCardAdapter(new ArrayList<>(), this);
        hotBooksRecyclerView.setAdapter(hotBooksAdapter);

        // コンテナやヘッダー設定
        recommendedBooksContainer.setVisibility(View.VISIBLE);
        matchingBooksHeader.setVisibility(View.VISIBLE);
        nonMatchingBooksHeader.setVisibility(View.VISIBLE);
        hotBooksHeader.setVisibility(View.VISIBLE);
        noMatchingBooksMessage.setVisibility(View.GONE);
        noNonMatchingBooksMessage.setVisibility(View.GONE);
        noHotBooksMessage.setVisibility(View.GONE);
    }

    /**
     * ユーザー情報処理部(UserFeatureProcessor)を経由して、ユーザーの好きなジャンルを取得し、
     * その後、本の情報処理部(BookFeatureProcessor)を経由しておすすめ本を取得・表示します。
     *
     * @param userId 現在のユーザーID
     */
    private void fetchUserFavoriteGenres(String userId) {
        userFeatureProcessor.fetchUserFavoriteGenres(userId, new UserFeatureProcessor.UserGenresCallback() {
            /**
             * ユーザーの好きなジャンルが正常に取得されたときに呼び出されます。
             * @param genres 取得されたジャンルのリスト
             */
            @Override
            public void onGenresReceived(List<String> genres) {
                Log.d("MainActivity", "ユーザーの好きなジャンル: " + genres);
                fetchAndDisplayRecommendedBooksFromFirestore(genres);
            }

            /**
             * ユーザーの好きなジャンルの取得中にエラーが発生したときに呼び出されます。
             * @param errorMessage エラーメッセージ
             */
            @Override
            public void onFailure(String errorMessage) {
                Log.e("MainActivity", "好きなジャンルの取得エラー: " + errorMessage);
                mainHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "おすすめジャンルの取得に失敗しました: " + errorMessage, Toast.LENGTH_LONG).show();
                    matchingBooksAdapter.setBookList(new ArrayList<>());
                    nonMatchingBooksAdapter.setBookList(new ArrayList<>());
                    noMatchingBooksMessage.setText("ジャンル取得エラー: " + errorMessage);
                    noMatchingBooksMessage.setVisibility(View.VISIBLE);
                    noNonMatchingBooksMessage.setText("ジャンル取得エラー: " + errorMessage);
                    noNonMatchingBooksMessage.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    /**
     * 本の情報処理部(BookFeatureProcessor)を経由して、Firestoreからおすすめの書籍を取得・表示します。
     *
     * @param favoriteGenres ユーザーが好きなジャンルのリスト
     */
    private void fetchAndDisplayRecommendedBooksFromFirestore(List<String> favoriteGenres) {
        bookFeatureProcessor.fetchRecommendedBooks(favoriteGenres, new BookFeatureProcessor.RecommendedBooksCallback() {
            /**
             * おすすめ書籍リストが正常に取得されたときに呼び出されます。
             * @param matchingBooks ユーザーの好きなジャンルに一致する書籍のリスト
             * @param nonMatchingBooks ユーザーの好きなジャンルに一致しない書籍のリスト
             */
            @Override
            public void onRecommendationsReceived(List<Book> matchingBooks, List<Book> nonMatchingBooks) {
                Log.d("MainActivity", "Firestoreから取得した合う本 (" + matchingBooks.size() + "冊): " + matchingBooks.stream().map(Book::getTitle).collect(Collectors.joining(", ")));
                Log.d("MainActivity", "Firestoreから取得した合わない本 (" + nonMatchingBooks.size() + "冊): " + nonMatchingBooks.stream().map(Book::getTitle).collect(Collectors.joining(", ")));

                mainHandler.post(() -> {
                    // ユーザーの好みに合う本の表示更新
                    matchingBooksHeader.setVisibility(View.VISIBLE);
                    matchingBooksRecyclerView.setVisibility(View.VISIBLE);

                    if (!matchingBooks.isEmpty()) {
                        matchingBooksAdapter.setBookList(matchingBooks);
                        noMatchingBooksMessage.setVisibility(View.GONE);
                    } else {
                        matchingBooksAdapter.setBookList(new ArrayList<>());
                        noMatchingBooksMessage.setText("表示する本がありません。");
                        noMatchingBooksMessage.setVisibility(View.VISIBLE);
                    }

                    // ユーザーの好みと異なる本の表示更新
                    nonMatchingBooksHeader.setVisibility(View.VISIBLE);
                    nonMatchingBooksRecyclerView.setVisibility(View.VISIBLE);

                    if (!nonMatchingBooks.isEmpty()) {
                        nonMatchingBooksAdapter.setBookList(nonMatchingBooks);
                        noNonMatchingBooksMessage.setVisibility(View.GONE);
                    } else {
                        nonMatchingBooksAdapter.setBookList(new ArrayList<>());
                        noNonMatchingBooksMessage.setText("表示する本がありません。");
                        noNonMatchingBooksMessage.setVisibility(View.VISIBLE);
                    }
                });
            }

            /**
             * おすすめ書籍の取得中にエラーが発生したときに呼び出されます。
             * @param errorMessage エラーメッセージ
             */
            @Override
            public void onFailure(String errorMessage) {
                Log.e("MainActivity", "Firestoreからおすすめ本の取得エラー: " + errorMessage);
                mainHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "おすすめ本の取得に失敗しました: " + errorMessage, Toast.LENGTH_LONG).show();
                    matchingBooksAdapter.setBookList(new ArrayList<>());
                    noMatchingBooksMessage.setText("本の取得中にエラーが発生しました。");
                    noMatchingBooksMessage.setVisibility(View.VISIBLE);

                    nonMatchingBooksAdapter.setBookList(new ArrayList<>());
                    noNonMatchingBooksMessage.setText("本の取得中にエラーが発生しました。");
                    noNonMatchingBooksMessage.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    /**
     * 管理部(BookAppManager)を経由して楽天ランキングの書籍を取得・表示します。
     */
    private void fetchRakutenRankingBooks() {
        bookAppManager.fetchHotBooks(new BookAppManager.HotBooksCallback() {
            /**
             * 話題の書籍リストが正常に取得されたときに呼び出されます。
             * @param hotBooks 話題の書籍のリスト
             */
            @Override
            public void onHotBooksReceived(List<Book> hotBooks) {
                mainHandler.post(() -> {
                    hotBooksHeader.setVisibility(View.VISIBLE);
                    hotBooksRecyclerView.setVisibility(View.VISIBLE);

                    if (!hotBooks.isEmpty()) {
                        hotBooksAdapter.setBookList(hotBooks);
                        noHotBooksMessage.setVisibility(View.GONE);
                    } else {
                        hotBooksAdapter.setBookList(new ArrayList<>());
                        noHotBooksMessage.setText("現在、話題の本はありません。");
                        noHotBooksMessage.setVisibility(View.VISIBLE);
                    }
                });
            }

            /**
             * 話題の書籍の取得中にエラーが発生したときに呼び出されます。
             * @param errorMessage エラーメッセージ
             */
            @Override
            public void onFailure(String errorMessage) {
                mainHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "話題の本の取得に失敗しました: " + errorMessage, Toast.LENGTH_LONG).show();
                    hotBooksAdapter.setBookList(new ArrayList<>());
                    noHotBooksMessage.setText("本の取得中にエラーが発生しました。");
                    noHotBooksMessage.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    /**
     * 書籍カードがクリックされたときに呼び出されるコールバックメソッドです。
     * 選択された書籍の詳細に基づいて、レビュー表示画面 (UserReviewListActivity) へ遷移します。
     *
     * @param book クリックされた書籍オブジェクト
     */
    @Override
    public void onBookClick(Book book) {
        Toast.makeText(this, "「" + book.getTitle() + "」の全体まとめを表示", Toast.LENGTH_SHORT).show();
        navigateToReviewActivity(book);
    }

    /**
     * 指定された書籍の情報をUserReviewListActivityに渡し、画面遷移を行います。
     *
     * @param book 遷移先のActivityに渡す書籍オブジェクト
     */
    private void navigateToReviewActivity(Book book) {
        Intent intent = new Intent(MainActivity.this, UserReviewListActivity.class);
        // Intentに書籍情報をextraとして追加
        intent.putExtra("bookId", book.getId());
        intent.putExtra("bookTitle", book.getTitle());
        intent.putExtra("book_author", book.getAuthor());
        intent.putExtra("book_description", book.getDescription());
        intent.putExtra("book_thumbnail_url", book.getThumbnailUrl());
        startActivity(intent);
    }
}
