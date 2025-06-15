/**
 * モジュール名: BookDetailData
 * 作成者: 横山葉
 * 作成日: 2025/06/9
 * 概要: 書籍の詳細情報を保持するデータクラス
 * 履歴:
 * 2025/06/9 横山葉 新規作成
 */

package com.example.bookup03;

/**
 * 書籍の詳細情報を表すクラス
 */
public class BookDetailData {
    /**
     * 書籍のボリュームID
     */
    private String volumeId;

    /**
     * 書籍名
     */
    private String name;

    /**
     * 書籍のあらすじ
     */
    private String summary;

    /**
     * カバー画像のURL
     */
    private String coverImageUrl;

    /**
     * 公開ステータス ("public" または "private")
     */
    private String publicStatus;

    /**
     * 書籍の詳細データを初期化する
     * @param volumeId ボリュームID
     * @param name 書籍名
     * @param summary あらすじ
     * @param coverImageUrl カバー画像のURL
     * @param publicStatus 公開ステータス
     */
    public BookDetailData(String volumeId, String name, String summary, String coverImageUrl, String publicStatus) {
        this.volumeId = volumeId;
        this.name = name;
        this.summary = summary;
        this.coverImageUrl = coverImageUrl;
        this.publicStatus = publicStatus;
    }

    /**
     * ボリュームIDを取得する
     * @return volumeId
     */
    public String getVolumeId() {
        return volumeId;
    }

    /**
     * 書籍名を取得する
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * あらすじを取得する
     * @return summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * カバー画像URLを取得する
     * @return coverImageUrl
     */
    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    /**
     * 公開ステータスを取得する
     * @return publicStatus
     */
    public String getPublicStatus() {
        return publicStatus;
    }
}