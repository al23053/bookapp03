package com.example.bookapp03.C1UIProcessing;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * ControlButtonToSummary の単体テスト
 * – ブラックボックス: startActivity が呼ばれること
 * – ホワイトボックス: Intentの遷移先を検証
 */
@RunWith(MockitoJUnitRunner.class)
public class ControlButtonToSummaryTest {

    @Mock
    private Context mockContext;
    @Captor
    private ArgumentCaptor<Intent> intentCaptor;

    private ControlButtonToSummary controller;

    @Before
    public void setUp() {
        controller = new ControlButtonToSummary();
    }

    @Test
    public void testSetToSummary_startsDisplaySummary() {
        controller.setToSummary(mockContext);

        // ブラックボックス: 1 回だけ startActivity が呼ばれる
        verify(mockContext).startActivity(any(Intent.class));

        // ホワイトボックス: Intent のクラス名を検証
        verify(mockContext).startActivity(intentCaptor.capture());
        String actual = intentCaptor.getValue().getComponent().getClassName();
        String expected = DisplaySummary.class.getName();
        assertEquals(expected, actual);
    }
}