package com.example.bookapp03.service;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Firebase Firestoreからユーザーのジャンル情報を取得・保存するためのサービスです。
 * ユーザーの好きなジャンルに関するデータベース操作をカプセル化します。
 */
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
        db.collection("users") // 'users' コレクションにアクセス
                .document(userId) // 特定のユーザーIDのドキュメントにアクセス
                .get() // ドキュメントを取得
                .addOnCompleteListener(task -> { // 取得完了時のリスナーを設定
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Firestoreドキュメントから'favoriteGenres'フィールドを取得
                            // フィールド名が 'favoriteGenres' で List<String> 型であると仮定しています。
                            List<String> genres = (List<String>) document.get("genre");
                            if (genres == null) {
                                genres = new ArrayList<>(); // フィールドが存在しない、またはnullの場合は空のリストを返す
                            }
                            Log.d(TAG, "Fetched genres for user " + userId + ": " + genres);
                            callback.onGenresReceived(genres); // 正常に取得できた場合、コールバックでジャンルリストを通知
                        } else {
                            Log.d(TAG, "No such document for user: " + userId);
                            callback.onGenresReceived(new ArrayList<>()); // ドキュメントが存在しない場合は空のリストを返す
                        }
                    } else {
                        // 取得中にエラーが発生した場合
                        String errorMessage = "Failed to fetch user genres: " + task.getException().getMessage();
                        Log.e(TAG, errorMessage, task.getException());
                        callback.onFailure(errorMessage); // エラーメッセージをコールバックで通知
                    }
                });
    }
}
