package com.example.bookapp03.C4SearchProcessing;

import android.util.Log;

import com.example.bookapp03.manager.BookAppManager; // 管理部をインポート
import com.example.bookapp03.model.Book;

import java.util.List;

/**
 * 検索機能に特化したビジネスロジックを処理するクラス。
 * UIと管理部の間に位置し、検索に関する特定の振る舞いをカプセル化します。
 */
public class SearchFeatureProcessor {

    private static final String TAG = "SearchFeatureProcessor";

    private final BookAppManager bookAppManager; // 検索管理部への参照

    public interface SuggestionsCallback {
        void onSuggestionsReceived(List<String> suggestions);

        void onFailure(String errorMessage);
    }

    public interface SearchCallback {
        void onSearchResultsReceived(List<Book> books);

        void onFailure(String errorMessage);
    }

    /**
     * コンストラクタ。BookAppManagerのインスタンスを注入します。
     *
     * @param bookAppManager 初期化済みのBookAppManagerインスタンス
     */
    public SearchFeatureProcessor(BookAppManager bookAppManager) {
        this.bookAppManager = bookAppManager;
        Log.d(TAG, "SearchFeatureProcessor initialized.");
    }

    /**
     * 検索候補の取得を管理部へ委譲します。
     * ここで検索候補に関する追加の処理ロジック（例: キャッシュの利用、頻繁なリクエストの抑制など）を実装できます。
     *
     * @param query    検索クエリ
     * @param callback 結果を返すコールバック
     */
    public void fetchSuggestions(String query, final SuggestionsCallback callback) {
        if (query == null || query.trim().isEmpty()) {
            callback.onSuggestionsReceived(List.of()); // Java 9+の場合、Collections.emptyList() でもOK
            return;
        }

        bookAppManager.fetchSuggestions(query, new BookAppManager.SuggestionsCallback() {
            @Override
            public void onSuggestionsReceived(List<String> suggestions) {
                // 必要であれば、ここで検索候補に対する追加の処理
                callback.onSuggestionsReceived(suggestions);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Failed to fetch suggestions from manager: " + errorMessage);
                callback.onFailure(errorMessage);
            }
        });
    }

    /**
     * 書籍検索を管理部へ委譲します。
     * ここで検索実行に関する追加の処理ロジック（例: 検索履歴の記録、複雑な検索条件の構築など）を実装できます。
     *
     * @param query    検索クエリ
     * @param callback 結果を返すコールバック
     */
    public void performSearch(String query, final SearchCallback callback) {
        if (query == null || query.trim().isEmpty()) {
            callback.onFailure("検索キーワードが入力されていません。");
            return;
        }
        Log.d(TAG, "SearchFeatureProcessor: Processing search for query: " + query);

        bookAppManager.searchBooks(query, new BookAppManager.SearchCallback() {
            @Override
            public void onSearchResultsReceived(List<Book> books) {
                callback.onSearchResultsReceived(books);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Failed to perform search from manager: " + errorMessage);
                callback.onFailure(errorMessage);
            }
        });
    }
}