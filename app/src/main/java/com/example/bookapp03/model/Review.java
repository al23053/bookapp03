package com.example.bookapp03.model; // あなたのパッケージ名に合わせる

/**
 * 書籍レビューの情報を表現するモデルクラスです。
 * ユーザーID、ユーザー名、評価、コメント、およびレビュー作成日時を格納します。
 * Firebase Firestoreへの保存に適した構造になっています。
 */
public class Review {
    /**
     * レビューのコメント本文または要約。Firestoreの 'overallSummary' フィールドに対応。
     */
    private String overallSummary;
    /**
     * レビューを投稿したユーザーの一意なID。Firestoreの 'uid' フィールドに対応。
     * このIDを使用して 'users' コレクションからユーザー名を取得します。
     */
    private String uid;
    /**
     * レビューが関連付けられている書籍のID。Firestoreの 'volumeId' フィールドに対応。
     */
    private String volumeId;

    /**
     * レビューが公開されているかどうかを示すフラグ。Firestoreの 'isPublic' フィールドに対応。
     */
    private boolean isPublic;

    /**
     * レビューがFirestoreサーバーに保存された際のタイムスタンプ。
     *
     * @ServerTimestamp アノテーションにより、Firestoreが自動的にこのフィールドにサーバーのタイムスタンプを設定・更新します。
     */

    /**
     * FirebaseFirestoreがオブジェクトを自動的に変換するために必須となる、引数なしのデフォルトコンストラクタです。
     */
    public Review() {
        // Default constructor required for calls to DataSnapshot.getValue(Review.class)
    }

    /**
     * 新しいレビューを作成するためのコンストラクタです。
     * Firestoreの 'summaries' コレクションのフィールドに合わせます。
     *
     * @param overallSummary レビューのコメント本文または要約
     * @param uid            レビューを投稿するユーザーのID
     * @param volumeId       レビューが関連付けられている書籍のID
     * @param isPublic       レビューが公開されているかどうかのフラグ
     */
    public Review(String overallSummary, String uid, String volumeId, boolean isPublic) {
        this.overallSummary = overallSummary;
        this.uid = uid;
        this.volumeId = volumeId;
        this.isPublic = isPublic;
    }

    // GetterとSetter (必須)

    /**
     * レビューのコメント本文または要約を取得します。
     *
     * @return コメント本文または要約
     */
    public String getOverallSummary() {
        return overallSummary;
    }

    /**
     * レビューのコメント本文または要約を設定します。
     *
     * @param overallSummary 設定するコメント本文または要約
     */
    public void setOverallSummary(String overallSummary) {
        this.overallSummary = overallSummary;
    }

    /**
     * レビューを投稿したユーザーのIDを取得します。
     *
     * @return ユーザーID (uid)
     */
    public String getUid() {
        return uid;
    }

    /**
     * レビューを投稿したユーザーのIDを設定します。
     *
     * @param uid 設定するユーザーID
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * レビューが関連付けられている書籍のIDを取得します。
     *
     * @return 書籍ID (volumeId)
     */
    public String getVolumeId() {
        return volumeId;
    }

    /**
     * レビューが関連付けられている書籍のIDを設定します。
     *
     * @param volumeId 設定する書籍ID
     */
    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    /**
     * レビューが公開されているかどうかを示すフラグを取得します。
     *
     * @return trueなら公開、falseなら非公開
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * レビューが公開されているかどうかを示すフラグを設定します。
     *
     * @param aPublic 設定する公開フラグ
     */
    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    // 既存のUserReviewListActivityで使用されているメソッド名に合わせるためのエイリアス
    // (UIロジックの修正を最小限にするため)
    public String getComment() {
        return overallSummary;
    }
}
