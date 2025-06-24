package com.example.bookapp03.C1UIProcessing;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.content.Intent;

import com.example.bookapp03.C1UIProcessing.ControlBottomNavigationBar;
import com.example.bookapp03.C1UIProcessing.DisplayHome;
import com.example.bookapp03.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * ControlBottomNavigationBar の単体テスト
 * - nav_home 選択時の遷移
 * - 未知の IDの場合に遷移しないこと
 */
@RunWith(MockitoJUnitRunner.class)
public class ControlBottomNavigationBarTest {

    @Mock
    private Context mockContext;

    @Captor
    private ArgumentCaptor<Intent> intentCaptor;

    private ControlBottomNavigationBar controller;

    @Before
    public void setUp() {
        controller = new ControlBottomNavigationBar();
    }

    /**
     * menuId == R.id.nav_home の場合に DisplayHome へ遷移する。
     */
    @Test
    public void testHandledisplay_home() {
        controller.handledisplay(R.id.nav_home, mockContext);

        // ブラックボックス: startActivity 呼び出し
        verify(mockContext).startActivity(any(Intent.class));

        // ホワイトボックス: Intent のターゲットクラスを検証
        verify(mockContext).startActivity(intentCaptor.capture());
        Intent intent = intentCaptor.getValue();
        String actual = intent.getComponent().getClassName();
        String expected = DisplayHome.class.getName();
        assertEquals(expected, actual);
    }

    /**
     * 未定義の menuId の場合は startActivity が呼ばれない。
     */
    @Test
    public void testHandledisplay_unknown() {
        controller.handledisplay(-999, mockContext);

        // ブラックボックス: startActivity が一切呼ばれない
        verify(mockContext, never()).startActivity(any(Intent.class));
    }
}