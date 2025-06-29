package com.example.bookapp03.model; // あなたのパッケージ名に合わせる

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp; // タイムスタンプ自動生成用

/**
 * 書籍レビューの情報を表現するモデルクラスです。
 * ユーザーID、ユーザー名、評価、コメント、およびレビュー作成日時を格納します。
 * Firebase Firestoreへの保存に適した構造になっています。
 */
public class Review {
    /**
     * レビューを投稿したユーザーの一意なID。
     */
    private String userId;

    /**
     * レビューを投稿したユーザーの表示名。
     */
    private String username;

    /**
     * 書籍に対する評価。通常、1.0fから5.0fの範囲の浮動小数点数です。
     */
    private float rating; // 1.0f から 5.0f

    /**
     * レビューのコメント本文。
     */
    private String comment;

    /**
     * レビューがFirestoreサーバーに保存された際のタイムスタンプ。
     *
     * @ServerTimestamp アノテーションにより、Firestoreが自動的にこのフィールドにサーバーのタイムスタンプを設定・更新します。
     */
    @ServerTimestamp // Firestoreが自動的にタイムスタンプを生成・更新
    private Timestamp timestamp;

    /**
     * FirebaseFirestoreがオブジェクトを自動的に変換するために必須となる、引数なしのデフォルトコンストラクタです。
     */
    public Review() {
        // Default constructor required for calls to DataSnapshot.getValue(Review.class)
    }

    /**
     * 新しいレビューを作成するためのコンストラクタです。
     * タイムスタンプはFirestoreによって自動的に設定されます。
     *
     * @param userId   レビューを投稿するユーザーのID
     * @param username レビューを投稿するユーザーの表示名
     * @param rating   書籍に対する評価（1.0f〜5.0f）
     * @param comment  レビューのコメント本文
     */
    public Review(String userId, String username, float rating, String comment) {
        this.userId = userId;
        this.username = username;
        this.rating = rating;
        this.comment = comment;
        // timestampは@ServerTimestampで自動設定されるため、ここでは設定しない
    }

    // GetterとSetter (必須)

    /**
     * レビューを投稿したユーザーのIDを取得します。
     *
     * @return ユーザーID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * レビューを投稿したユーザーのIDを設定します。
     *
     * @param userId 設定するユーザーID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * レビューを投稿したユーザーの表示名を取得します。
     *
     * @return ユーザー名
     */
    public String getUsername() {
        return username;
    }

    /**
     * レビューを投稿したユーザーの表示名を設定します。
     *
     * @param username 設定するユーザー名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 書籍に対する評価を取得します。
     *
     * @return 評価（float型）
     */
    public float getRating() {
        return rating;
    }

    /**
     * 書籍に対する評価を設定します。
     *
     * @param rating 設定する評価（1.0f〜5.0f）
     */
    public void setRating(float rating) {
        this.rating = rating;
    }

    /**
     * レビューのコメント本文を取得します。
     *
     * @return コメント本文
     */
    public String getComment() {
        return comment;
    }

    /**
     * レビューのコメント本文を設定します。
     *
     * @param comment 設定するコメント本文
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * レビューがFirestoreに保存された際のタイムスタンプを取得します。
     *
     * @return レビューのタイムスタンプ
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }
}
