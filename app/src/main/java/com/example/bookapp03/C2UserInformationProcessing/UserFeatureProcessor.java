/**
 * モジュール名: UserFeatureProcessor
 * 作成者: 三浦寛生
 * 作成日: 2025/06/15
 * 概要:　ユーザー情報に関するビジネスロジックを処理する層。
 * UI層からのリクエストを受け取り、管理部を経由してデータサービスにアクセスします。
 * 履歴:
 * 2025/06/15 三浦寛生 新規作成
 */
package com.example.bookapp03.C2UserInformationProcessing;

import android.util.Log;

import com.example.bookapp03.C7SearchManaging.BookAppManager;

import java.util.List;

public class UserFeatureProcessor {

    private static final String TAG = "UserFeatureProcessor";

    private final BookAppManager bookAppManager;

    public interface UserGenresCallback {
        void onGenresReceived(List<String> genres);

        void onFailure(String errorMessage);
    }

    /**
     * コンストラクタ。BookAppManagerのインスタンスを注入します。
     *
     * @param bookAppManager 初期化済みのBookAppManagerインスタンス
     */
    public UserFeatureProcessor(BookAppManager bookAppManager) {
        this.bookAppManager = bookAppManager;
        Log.d(TAG, "UserFeatureProcessor initialized.");
    }

    /**
     * ユーザーの好きなジャンルを取得する処理を管理部へ委譲します。
     *
     * @param userId   ユーザーID
     * @param callback 結果を返すコールバック
     */
    public void fetchUserFavoriteGenres(String userId, final UserGenresCallback callback) {
        Log.d(TAG, "UserFeatureProcessor: Fetching user favorite genres for userId: " + userId);

        bookAppManager.fetchUserFavoriteGenres(userId, new BookAppManager.UserGenresCallback() {
            @Override
            public void onGenresReceived(List<String> genres) {
                callback.onGenresReceived(genres);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Failed to fetch user genres from manager: " + errorMessage);
                callback.onFailure(errorMessage);
            }
        });
    }
}