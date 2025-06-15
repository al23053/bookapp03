/**
 * モジュール名: BookSummaryData
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍のサマリ情報を保持するデータクラス
 * 履歴:
 * 2025/06/09 横山葉 新規作成
 */

package com.example.bookup03;

/**
 * 書籍の概要情報を管理するクラス
 */
public class BookSummaryData {
    private String volumeId;
    private String title;
    private String imageUrl;
    private boolean isPublic;

    /**
     * コンストラクタ
     * @param volumeId 書籍ID
     * @param title タイトル
     * @param imageUrl 画像URL
     */
    public BookSummaryData(String volumeId, String title, String imageUrl) {
        this.volumeId = volumeId;
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}
