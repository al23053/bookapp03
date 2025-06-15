/**
 * モジュール名: HighlightMemoData
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: ハイライトメモのデータを保持するクラス
 * 履歴:
 * 2025/06/09 横山葉 新規作成
 */

package com.example.bookup03.data;

/**
 * ハイライトメモの情報を管理するデータクラス
 */
public class HighlightMemoData {
    private String page;
    private String line;
    private String memoContent;

    /**
     * コンストラクタ
     * @param page ページ番号
     * @param line 行番号
     * @param memoContent メモ内容
     */
    public HighlightMemoData(String page, String line, String memoContent) {
        this.page = page;
        this.line = line;
        this.memoContent = memoContent;
    }

    public String getPage() {
        return page;
    }

    public String getLine() {
        return line;
    }

    public String getMemoContent() {
        return memoContent;
    }
}