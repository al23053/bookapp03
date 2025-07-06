/**
 * モジュール名: 本の概要データクラス
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍のサマリ情報を保持するデータクラス。
 * 履歴:
 * 2025/06/09 横山葉 新規作成
 */
package com.example.bookapp03.C3BookInformationProcessing;

/**
 * 書籍の概要情報を保持するデータクラス。
 * 書籍ID、タイトル、画像URL、および公開状態などのサマリ情報を管理します。
 */
public class BookSummaryData {
    /** 書籍のGoogle Books APIにおけるボリュームID */
    private String volumeId;
    /** 書籍のタイトル */
    private String title;
    /** 書籍のカバー画像のURL */
    private String imageUrl;
    /** 書籍が公開状態であるかどうかのフラグ (true: 公開, false: 非公開) */
    private boolean isPublic;

    /**
     * BookSummaryDataのコンストラクタ。
     * 新しい書籍サマリデータを初期化します。
     *
     * @param volumeId 書籍のボリュームID
     * @param title    書籍のタイトル
     * @param imageUrl 書籍のカバー画像URL
     */
    public BookSummaryData(String volumeId, String title, String imageUrl) {
        this.volumeId = volumeId;
        this.title = title;
        this.imageUrl = imageUrl;
        // 初期状態では公開設定をfalse（非公開）としています。必要に応じて調整してください。
        this.isPublic = false;
    }

    /**
     * 書籍のボリュームIDを取得します。
     *
     * @return 書籍のボリュームID
     */
    public String getVolumeId() {
        return volumeId;
    }

    /**
     * 書籍のタイトルを取得します。
     *
     * @return 書籍のタイトル
     */
    public String getTitle() {
        return title;
    }

    /**
     * 書籍のカバー画像URLを取得します。
     *
     * @return 書籍のカバー画像URL
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * 書籍が公開状態であるかどうかを返します。
     *
     * @return 書籍が公開状態であればtrue、非公開であればfalse
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * 書籍の公開状態を設定します。
     *
     * @param aPublic 書籍を公開状態にする場合はtrue、非公開にする場合はfalse
     */
    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
}