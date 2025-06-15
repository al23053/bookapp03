/**
 * モジュール名: BookDetailViewModel
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍詳細データの表示用ビューモデル
 * 履歴:
 *   2025/06/09 横山葉 新規作成
 */

package com.example.bookup03;

/**
 * 書籍の詳細情報を保持・管理するビューモデル
 */
public class BookDetailViewModel {

    /**
     * 表示中の書籍詳細データ
     */
    private BookDetailData detail;

    /**
     * 書籍詳細データを取得する
     * @return BookDetailData 書籍の詳細データ
     */
    public BookDetailData getDetail() {
        return detail;
    }

    /**
     * 書籍詳細データを設定する
     * @param detail 書籍の詳細データ
     */
    public void setDetail(BookDetailData detail) {
        this.detail = detail;
    }

    /**
     * 書籍詳細データが存在するかどうかを確認する
     * @return データが存在する場合 true、存在しない場合 false
     */
    public boolean hasDetail() {
        return detail != null;
    }
}