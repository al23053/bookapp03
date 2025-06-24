package com.example.bookapp03.C1UIProcessing;

import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;

import com.example.bookapp03.C1UIProcessing.ControlBackToHomeButton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * ControlBackToHomeButton の単体テスト
 * - setOnClickListenerが呼ばれること
 * - クリックで Activity.finish() が呼ばれること
 */
@RunWith(MockitoJUnitRunner.class)
public class ControlBackToHomeButtonTest {

    @Mock
    private Activity mockActivity;

    @Mock
    private ImageButton mockButton;

    @Captor
    private ArgumentCaptor<View.OnClickListener> listenerCaptor;

    private ControlBackToHomeButton controller;

    @Before
    public void setUp() {
        controller = new ControlBackToHomeButton(mockActivity);
        controller.bind(mockButton);
    }

    /**
     * bind() 後に ImageButton#setOnClickListener が呼ばれる。
     */
    @Test
    public void testBind_setsOnClickListener() {
        // ブラックボックス: リスナー設定を検証
        verify(mockButton).setOnClickListener(listenerCaptor.capture());
    }

    /**
     * 設定されたリスナーを呼び出すと Activity.finish() が呼ばれる。
     */
    @Test
    public void testOnClick_callsFinish() {
        // ホワイトボックス: setOnClickListener で渡されたリスナーを取得
        verify(mockButton).setOnClickListener(listenerCaptor.capture());
        View.OnClickListener listener = listenerCaptor.getValue();

        // クリックをシミュレート
        listener.onClick(null);

        // Activity.finish() 呼び出しを検証
        verify(mockActivity).finish();
    }
}