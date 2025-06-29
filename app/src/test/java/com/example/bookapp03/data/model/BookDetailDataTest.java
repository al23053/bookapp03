package com.example.bookapp03.data.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.example.bookapp03.C3BookInformationProcessing.BookDetailData;

/**
 * BookDetailDataの単体テスト (JUnit 4)
 */
public class BookDetailDataTest {

    private BookDetailData bookDetailData;

    @Before
    public void setUp() {
        bookDetailData = new BookDetailData(
                "vol_id_123",
                "JUnit 4 テスト書籍",
                "これはJUnit 4のテスト用の書籍あらすじです。",
                "http://example.com/cover_junit4.jpg",
                "public"
        );
    }

    @Test
    public void testConstructorAndGetters_validArguments() {
        BookDetailData data = new BookDetailData(
                "new_vol_id",
                "新しい書籍",
                "新しいあらすじ",
                "http://new.com/image.png",
                "private"
        );

        assertNotNull("BookDetailDataオブジェクトがnullではないこと", data);
        assertEquals("ボリュームIDが正しく設定されていること", "new_vol_id", data.getVolumeId());
        assertEquals("書籍名が正しく設定されていること", "新しい書籍", data.getName());
        assertEquals("あらすじが正しく設定されていること", "新しいあらすじ", data.getSummary());
        assertEquals("カバー画像URLが正しく設定されていること", "http://new.com/image.png", data.getCoverImageUrl());
        assertEquals("公開ステータスが正しく設定されていること", "private", data.getPublicStatus());
    }

    @Test
    public void testGetVolumeId() {
        assertEquals("getVolumeId()が正しいボリュームIDを返すこと", "vol_id_123", bookDetailData.getVolumeId());
    }

    @Test
    public void testGetName() {
        assertEquals("getName()が正しい書籍名を返すこと", "JUnit 4 テスト書籍", bookDetailData.getName());
    }

    @Test
    public void testGetSummary() {
        assertEquals("getSummary()が正しいあらすじを返すこと", "これはJUnit 4のテスト用の書籍あらすじです。", bookDetailData.getSummary());
    }

    @Test
    public void testGetCoverImageUrl() {
        assertEquals("getCoverImageUrl()が正しいカバー画像URLを返すこと", "http://example.com/cover_junit4.jpg", bookDetailData.getCoverImageUrl());
    }

    @Test
    public void testGetPublicStatus() {
        assertEquals("getPublicStatus()が正しい公開ステータスを返すこと", "public", bookDetailData.getPublicStatus());
    }

    // ブラックボックステスト（限界値分析・同値分割）
    @Test
    public void testConstructor_emptyStrings() {
        BookDetailData data = new BookDetailData("", "", "", "", "");
        assertEquals("ボリュームIDが空文字列でも正しく設定されること", "", data.getVolumeId());
        assertEquals("書籍名が空文字列でも正しく設定されること", "", data.getName());
        assertEquals("あらすじが空文字列でも正しく設定されること", "", data.getSummary());
        assertEquals("カバー画像URLが空文字列でも正しく設定されること", "", data.getCoverImageUrl());
        assertEquals("公開ステータスが空文字列でも正しく設定されること", "", data.getPublicStatus());
    }

    @Test
    public void testConstructor_nullStrings() {
        BookDetailData data = new BookDetailData(null, null, null, null, null);
        assertNull("ボリュームIDがnullでも正しく設定されること", data.getVolumeId());
        assertNull("書籍名がnullでも正しく設定されること", data.getName());
        assertNull("あらすじがnullでも正しく設定されること", data.getSummary());
        assertNull("カバー画像URLがnullでも正しく設定されること", data.getCoverImageUrl());
        assertNull("公開ステータスがnullでも正しく設定されること", data.getPublicStatus());
    }

    @Test
    public void testConstructor_privateStatus() {
        BookDetailData data = new BookDetailData("id", "name", "summary", "url", "private");
        assertEquals("公開ステータスが'private'でも正しく設定されること", "private", data.getPublicStatus());
    }

    @Test
    public void testConstructor_invalidStatus() {
        BookDetailData data = new BookDetailData("id", "name", "summary", "url", "unknown_status");
        assertEquals("公開ステータスが想定外の値でもそのまま設定されること", "unknown_status", data.getPublicStatus());
    }
}