/**
 * モジュール名: BookFeatureProcessor
 * 作成者: 三浦寛生
 * 作成日: 2025/06/15
 * 概要:　本の情報に関するビジネスロジックを処理する層。
 * UI層からのリクエストを受け取り、管理部を経由してデータサービスにアクセスします。
 * 履歴:
 * 2025/06/15 三浦寛生 新規作成
 */
package com.example.bookapp03.C3BookInformationProcessing;

import android.util.Log;

import com.example.bookapp03.manager.BookAppManager;
import com.example.bookapp03.model.Book;

import java.util.List;

public class BookFeatureProcessor {

    private static final String TAG = "BookFeatureProcessor";

    private final BookAppManager bookAppManager;

    public interface RecommendedBooksCallback {
        void onRecommendationsReceived(List<Book> matchingBooks, List<Book> nonMatchingBooks);

        void onFailure(String errorMessage);
    }

    /**
     * コンストラクタ。BookAppManagerのインスタンスを注入します。
     *
     * @param bookAppManager 初期化済みのBookAppManagerインスタンス
     */
    public BookFeatureProcessor(BookAppManager bookAppManager) {
        this.bookAppManager = bookAppManager;
        Log.d(TAG, "BookFeatureProcessor initialized.");
    }

    /**
     * おすすめの書籍（ユーザーの好きなジャンルに合う・合わない）を取得する処理を管理部へ委譲します。
     * ここで本の情報に関する追加のロジック（例：フィルター、ソート、キャッシュ利用など）を実装できます。
     *
     * @param favoriteGenres ユーザーが好きなジャンルのリスト
     * @param callback       結果を通知するコールバック
     */
    public void fetchRecommendedBooks(List<String> favoriteGenres, final RecommendedBooksCallback callback) {
        Log.d(TAG, "BookFeatureProcessor: Fetching recommended books based on genres: " + favoriteGenres);

        bookAppManager.getRecommendedBooksFromFirestore(favoriteGenres, new BookAppManager.RecommendedBooksCallback() {
            @Override
            public void onRecommendationsReceived(List<Book> matchingBooks, List<Book> nonMatchingBooks) {
                callback.onRecommendationsReceived(matchingBooks, nonMatchingBooks);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Failed to fetch recommended books from manager: " + errorMessage);
                callback.onFailure(errorMessage);
            }
        });
    }
}