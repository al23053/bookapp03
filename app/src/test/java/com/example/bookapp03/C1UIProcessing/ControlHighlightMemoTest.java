package com.example.bookapp03.C1UIProcessing;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * ControlHighlightMemo の単体テスト
 * – ブラックボックス: getHighlightMemoの正常系／異常系
 * – ホワイトボックス: 例外メッセージを検証
 */
@RunWith(MockitoJUnitRunner.class)
public class ControlHighlightMemoTest {

    private static final String UID = "user123";
    private static final String VID = "vol456";

    private Context mockContext;
    private ControlHighlightMemo controller;

    @Before
    public void setUp() {
        mockContext = Mockito.mock(Context.class);
        controller = new ControlHighlightMemo(mockContext, UID, VID);
    }

    @Test
    public void testGetHighlightMemo_valid() {
        HighlightMemoData data = controller.getHighlightMemo(10, 5, "hello");
        assertEquals(10, data.getPage());
        assertEquals(5, data.getLine());
        assertEquals("hello", data.getMemo());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetHighlightMemo_pageOutOfRange_throws() {
        controller.getHighlightMemo(0, 5, "memo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetHighlightMemo_lineOutOfRange_throws() {
        controller.getHighlightMemo(1, 0, "memo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetHighlightMemo_memoTooLong_throws() {
        String longMemo = new String(new char[201]).replace('\0', 'x');
        controller.getHighlightMemo(1, 1, longMemo);
    }
}