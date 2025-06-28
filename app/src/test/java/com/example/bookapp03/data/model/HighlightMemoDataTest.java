package com.example.bookapp03.data.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * HighlightMemoDataの単体テスト (JUnit 4)
 */
public class HighlightMemoDataTest {

    private HighlightMemoData highlightMemoData;

    // 各テストメソッドの前に実行されるセットアップ
    @Before
    public void setUp() {
        // テストデータとして共通のインスタンスを準備
        highlightMemoData = new HighlightMemoData(10, 5, "テストメモの内容");
    }

    @Test
    public void testConstructorAndGetters_validArguments() {
        // コンストラクタと各ゲッターのテスト
        // テスト対象のインスタンス化 (setUpで初期化済みだが、念のため個別のテストケースも用意)
        HighlightMemoData data = new HighlightMemoData(1, 2, "新しいメモ");

        assertNotNull("HighlightMemoDataオブジェクトがnullではないこと", data);
        assertEquals("ページ番号が正しく設定されていること", 1, data.getPage());
        assertEquals("行番号が正しく設定されていること", 2, data.getLine());
        assertEquals("メモ内容が正しく設定されていること", "新しいメモ", data.getMemoContent());
    }

    @Test
    public void testGetPage() {
        assertEquals("getPage()が設定されたページ番号を返すこと", 10, highlightMemoData.getPage());
    }

    @Test
    public void testGetLine() {
        assertEquals("getLine()が設定された行番号を返すこと", 5, highlightMemoData.getLine());
    }

    @Test
    public void testGetMemoContent() {
        assertEquals("getMemoContent()が設定されたメモ内容を返すこと", "テストメモの内容", highlightMemoData.getMemoContent());
    }

    // ブラックボックステスト（限界値分析・同値分割）
    @Test
    public void testConstructor_negativePage() {
        HighlightMemoData data = new HighlightMemoData(-1, 1, "負のページ番号");
        assertEquals("ページ番号が負の値でも正しく設定されること", -1, data.getPage());
    }

    @Test
    public void testConstructor_negativeLine() {
        HighlightMemoData data = new HighlightMemoData(1, -1, "負の行番号");
        assertEquals("行番号が負の値でも正しく設定されること", -1, data.getLine());
    }

    @Test
    public void testConstructor_emptyMemoContent() {
        HighlightMemoData data = new HighlightMemoData(1, 1, "");
        assertEquals("メモ内容が空文字列でも正しく設定されること", "", data.getMemoContent());
    }

    @Test
    public void testConstructor_nullMemoContent() {
        HighlightMemoData data = new HighlightMemoData(1, 1, null);
        assertNull("メモ内容がnullでも正しく設定されること", data.getMemoContent());
    }
}