package com.example.bookapp03.data.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.example.bookapp03.C3BookInformationProcessing.BookSummaryData;

/**
 * BookSummaryDataの単体テスト (JUnit 4)
 */
public class BookSummaryDataTest {

    private BookSummaryData bookSummaryData;

    @Before
    public void setUp() {
        bookSummaryData = new BookSummaryData("sum_id_456", "JUnit 4 サマリー", "http://example.com/summary_junit4.png");
    }

    @Test
    public void testConstructorAndGetters_validArguments() {
        BookSummaryData data = new BookSummaryData("new_sum_id", "新しいサマリー", "http://new.com/summary.png");

        assertNotNull("BookSummaryDataオブジェクトがnullではないこと", data);
        assertEquals("ボリュームIDが正しく設定されていること", "new_sum_id", data.getVolumeId());
        assertEquals("タイトルが正しく設定されていること", "新しいサマリー", data.getTitle());
        assertEquals("画像URLが正しく設定されていること", "http://new.com/summary.png", data.getImageUrl());
        assertFalse("isPublicの初期値がfalseであること", data.isPublic()); // コンストラクタで設定されないためデフォルト値
    }

    @Test
    public void testGetVolumeId() {
        assertEquals("getVolumeId()が正しいボリュームIDを返すこと", "sum_id_456", bookSummaryData.getVolumeId());
    }

    @Test
    public void testGetTitle() {
        assertEquals("getTitle()が正しいタイトルを返すこと", "JUnit 4 サマリー", bookSummaryData.getTitle());
    }

    @Test
    public void testGetImageUrl() {
        assertEquals("getImageUrl()が正しい画像URLを返すこと", "http://example.com/summary_junit4.png", bookSummaryData.getImageUrl());
    }

    @Test
    public void testIsPublic_initialValue() {
        assertFalse("isPublicの初期値がfalseであること", bookSummaryData.isPublic());
    }

    @Test
    public void testSetPublic() {
        // trueに設定
        bookSummaryData.setPublic(true);
        assertTrue("setPublic(true)でisPublic()がtrueを返すこと", bookSummaryData.isPublic());

        // falseに設定
        bookSummaryData.setPublic(false);
        assertFalse("setPublic(false)でisPublic()がfalseを返すこと", bookSummaryData.isPublic());
    }

    // ブラックボックステスト（限界値分析・同値分割）
    @Test
    public void testConstructor_emptyStrings() {
        BookSummaryData data = new BookSummaryData("", "", "");
        assertEquals("ボリュームIDが空文字列でも正しく設定されること", "", data.getVolumeId());
        assertEquals("タイトルが空文字列でも正しく設定されること", "", data.getTitle());
        assertEquals("画像URLが空文字列でも正しく設定されること", "", data.getImageUrl());
    }

    @Test
    public void testConstructor_nullStrings() {
        BookSummaryData data = new BookSummaryData(null, null, null);
        assertNull("ボリュームIDがnullでも正しく設定されること", data.getVolumeId());
        assertNull("タイトルがnullでも正しく設定されること", data.getTitle());
        assertNull("画像URLがnullでも正しく設定されること", data.getImageUrl());
    }
}