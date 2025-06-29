package com.example.bookapp03.C1UIProcessing;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.content.Intent;

import com.example.bookapp03.C1UIProcessing.ControlButtonToSetting;
import com.example.bookapp03.C1UIProcessing.DisplaySetting;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * ControlButtonToSetting の単体テスト
 * - ブラックボックス: startActivity が呼ばれること
 * - ホワイトボックス: Intentのクラス名を検証
 */
@RunWith(MockitoJUnitRunner.class)
public class ControlButtonToSettingTest {

    @Mock
    private Context mockContext;

    @Captor
    private ArgumentCaptor<Intent> intentCaptor;

    private ControlButtonToSetting controller;

    @Before
    public void setUp() {
        controller = new ControlButtonToSetting();
    }

    /**
     * setToSetting() 呼び出しで Context.startActivity() が一度だけ呼ばれ、
     * 遷移先が DisplaySetting であることを確認する。
     */
    @Test
    public void testSetToSetting_startsDisplaySetting() {
        controller.setToSetting(mockContext);

        // ブラックボックス: startActivity 呼び出しを検証
        verify(mockContext).startActivity(any(Intent.class));

        // ホワイトボックス: Intent の中身を検証
        verify(mockContext).startActivity(intentCaptor.capture());
        Intent intent = intentCaptor.getValue();
        String actual = intent.getComponent().getClassName();
        String expected = DisplaySetting.class.getName();
        assertEquals(expected, actual);
    }
}