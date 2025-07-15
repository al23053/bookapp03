/**
 * モジュール名: 本の詳細情報データクラス
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍の詳細情報を保持するデータクラス。
 * 履歴:
 * 2025/06/09 横山葉 新規作成
 */
package com.example.bookapp03.C3BookInformationProcessing;

/**
 * 書籍の詳細情報を表すデータクラス。
 * 書籍のボリュームID、タイトル、概要、カバー画像URL、公開ステータスなどの情報を保持します。
 */
public class BookDetailData {
    /**
     * 書籍のGoogle Books APIにおけるボリュームID。
     */
    private String volumeId;

    /**
     * 書籍のタイトルまたは書籍名。
     */
    private String name;

    /**
     * 書籍の全体まとめ（あらすじや要約）。
     */
    private String summary;

    /**
     * 書籍のカバー画像のURL。
     */
    private String coverImageUrl;

    /**
     * 書籍の公開ステータス。通常は "public" または "private" の文字列で表される。
     */
    private String publicStatus;

    /**
     * 書籍の詳細データを初期化するコンストラクタ。
     *
     * @param volumeId      書籍のボリュームID
     * @param name          書籍名
     * @param summary       書籍の全体まとめ（あらすじ）
     * @param coverImageUrl カバー画像のURL
     * @param publicStatus  公開ステータス ("public" または "private")
     */
    public BookDetailData(String volumeId, String name, String summary, String coverImageUrl, String publicStatus) {
        this.volumeId = volumeId;
        this.name = name;
        this.summary = summary;
        this.coverImageUrl = coverImageUrl;
        this.publicStatus = publicStatus;
    }

    /**
     * 書籍のボリュームIDを取得する。
     *
     * @return 書籍のボリュームID
     */
    public String getVolumeId() {
        return volumeId;
    }

    /**
     * 書籍名を取得する。
     *
     * @return 書籍名
     */
    public String getName() {
        return name;
    }

    /**
     * 全体まとめ（あらすじ）を取得する。
     *
     * @return 書籍の全体まとめ
     */
    public String getSummary() {
        return summary;
    }

    /**
     * カバー画像URLを取得する。
     *
     * @return カバー画像のURL
     */
    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    /**
     * 公開ステータスを日本語で取得する。
     * 内部的な "public" や "private" の値を、表示用の「公開」「非公開」に変換して返す。
     *
     * @return 日本語の公開ステータス（"公開"、"非公開"）、または不明な場合は元の文字列か"不明"
     */
    public String getPublicStatus() {
        if (this.publicStatus == null) {
            return "不明";
        } else if ("public".equalsIgnoreCase(this.publicStatus)) {
            return "公開";
        } else if ("private".equalsIgnoreCase(this.publicStatus)) {
            return "非公開";
        } else {
            return this.publicStatus;
        }
    }
}