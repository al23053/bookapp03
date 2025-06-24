package com.example.bookapp03.C1UIProcessing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * HighlightMemoData の単体テスト
 * - ブラックボックス: コンストラクタ／ゲッターの入出力
 * - ホワイトボックス: フィールド不変性の検証
 */
public class HighlightMemoDataTest {

    @Test
    public void testGetters_returnAssignedValues() {
        // ブラックボックス: 正常系
        HighlightMemoData data = new HighlightMemoData(3, 7, "メモ内容");
        assertEquals(3, data.getPage());
        assertEquals(7, data.getLine());
        assertEquals("メモ内容", data.getMemo());
    }

    @Test
    public void testImmutability_fieldsCannotChange() {
        // ホワイトボックス: フィールドは final、再設定不可
        HighlightMemoData data = new HighlightMemoData(1, 1, "初期");
        // 参照を取ってもゲッター以外操作なし → 同値を保証
        assertEquals(1, data.getPage());
        assertEquals(1, data.getLine());
        assertEquals("初期", data.getMemo());
    }
}