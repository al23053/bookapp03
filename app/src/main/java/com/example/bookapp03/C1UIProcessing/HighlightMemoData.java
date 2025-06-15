package com.example.bookapp03.C1UIProcessing;

/**
 * モジュール名: ハイライトメモデータ
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ハイライトメモのデータを保持するクラス
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */

public class HighlightMemoData {
    public int page;
    public int line;
    public String memo;

    public HighlightMemoData(int page, int line, String memo) {
        this.page = page;
        this.line = line;
        this.memo = memo;
    }

    public int getPage() {
        return page;
    }

    public int getLine() {
        return line;
    }

    public String getMemo() {
        return memo;
    }
}
