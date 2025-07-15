/**
 * モジュール名: UserGenreService
 * 作成者: 三浦寛生
 * 作成日: 2025/06/30
 * 概要:　Firebase Firestoreからユーザーのジャンル情報を取得・保存するためのサービスです。
 * ユーザーの好きなジャンルに関するデータベース操作をカプセル化します。
 * 履歴:
 * 2025/06/30 三浦寛生 新規作成
 */
package com.example.bookapp03.C5UserInformationManaging;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserGenreService {

    private static final String TAG = "UserGenreService";
    /**
     * Firebase Firestoreデータベースのインスタンス。
     */
    private FirebaseFirestore db;

    /**
     * UserGenreServiceのコンストラクタです。
     * FirebaseFirestoreのインスタンスを初期化します。
     */
    public UserGenreService() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * ユーザーの好きなジャンルの取得結果を通知するためのコールバックインターフェースです。
     */
    public interface UserGenresCallback {
        /**
         * ユーザーの好きなジャンルが正常に受信されたときに呼び出されます。
         *
         * @param genres ユーザーの好きなジャンルのリスト
         */
        void onGenresReceived(List<String> genres);

        /**
         * ユーザーの好きなジャンルの取得中にエラーが発生したときに呼び出されます。
         *
         * @param errorMessage エラーメッセージ
         */
        void onFailure(String errorMessage);
    }

    /**
     * 指定されたユーザーIDの好きなジャンルをFirestoreから非同期で取得します。
     *
     * @param userId   ユーザーの一意なID
     * @param callback 結果を通知するためのUserGenresCallback
     */
    public void getUserFavoriteGenres(String userId, UserGenresCallback callback) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<String> genres = (List<String>) document.get("genre");
                            if (genres == null) {
                                genres = new ArrayList<>();
                            }
                            Log.d(TAG, "Fetched genres for user " + userId + ": " + genres);
                            callback.onGenresReceived(genres);
                        } else {
                            Log.d(TAG, "No such document for user: " + userId);
                            callback.onGenresReceived(new ArrayList<>());
                        }
                    } else {
                        String errorMessage = "Failed to fetch user genres: " + task.getException().getMessage();
                        Log.e(TAG, errorMessage, task.getException());
                        callback.onFailure(errorMessage);
                    }
                });
    }
}
