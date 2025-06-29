package com.example.bookapp03.C1UIProcessing;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * ControlHamburgerBar の単体テスト
 * – ブラックボックス: bind() でリスナーがセットされる
 * – ホワイトボックス: リスナー実行後の Intent中身を検証
 */
@RunWith(MockitoJUnitRunner.class)
public class ControlHamburgerBarTest {

    private static final String UID = "user123";
    private static final String VID = "vol456";

    @Mock
    private Context mockContext;
    @Mock
    private ImageButton mockButton;
    @Captor
    private ArgumentCaptor<View.OnClickListener> listenerCaptor;

    @Before
    public void setUp() {
        ControlHamburgerBar controller = new ControlHamburgerBar(mockContext, UID, VID);
        controller.bind(mockButton);
    }

    @Test
    public void testBind_setsOnClickListener() {
        verify(mockButton).setOnClickListener(listenerCaptor.capture());
    }

    @Test
    public void testOnClick_startsDisplayHighlightMemo_withExtras() {
        verify(mockButton).setOnClickListener(listenerCaptor.capture());
        View.OnClickListener listener = listenerCaptor.getValue();

        // 押下をシミュレート
        listener.onClick(null);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(mockContext).startActivity(intentCaptor.capture());

        Intent intent = intentCaptor.getValue();
        assertEquals(DisplayHighlightMemo.class.getName(),
                     intent.getComponent().getClassName());
        assertEquals(UID, intent.getStringExtra("uid"));
        assertEquals(VID, intent.getStringExtra("volumeId"));
    }
}