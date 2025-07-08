package com.example.bookapp03.C1UIProcessing;

/**
 * モジュール名: ハイライトメモデータ
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ハイライトメモのページ番号・行番号・メモ文字列を保持するデータオブジェクト
 * 履歴:
 *   2025/06/15 鶴田凌 新規作成
 */
public class HighlightMemoData {

    /** ページ番号 */
    private final int page;

    /** 行番号 */
    private final int line;

    /** メモ文字列 */
    private final String memo;

    /**
     * コンストラクタ
     *
     * @param page ページ番号
     * @param line 行番号
     * @param memo メモ文字列
     */
    public HighlightMemoData(int page, int line, String memo) {
        this.page = page;
        this.line = line;
        this.memo = memo;
    }

    /**
     * ページ番号を取得する。
     *
     * @return ページ番号
     */
    public int getPage() {
        return page;
    }

    /**
     * 行番号を取得する。
     *
     * @return 行番号
     */
    public int getLine() {
        return line;
    }

    /**
     * メモ文字列を取得する。
     *
     * @return メモ文字列
     */
    public String getMemo() {
        return memo;
    }
}
