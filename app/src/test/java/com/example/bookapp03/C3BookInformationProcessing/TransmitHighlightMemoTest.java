package com.example.bookapp03.C3BookInformationProcessing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.example.bookapp03.C1UIProcessing.HighlightMemoData;
import com.example.bookapp03.C6BookInformationManaging.RegisterHighlightMemo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * TransmitHighlightMemoの単体テスト
 * – ブラックボックス: true/false の戻り値を検証
 * – ホワイトボックス: 内部で new RegisterHighlightMemo.registerHighlightMemo が呼ばれることを確認
 */
@RunWith(MockitoJUnitRunner.class)
public class TransmitHighlightMemoTest {

    @Mock
    private Context mockContext;

    private static final String UID       = "user1";
    private static final String VOLUME_ID = "vol1";
    private HighlightMemoData sampleData;

    @Before
    public void setUp() {
        // ApplicationContext 取得をスタブ化
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        sampleData = new HighlightMemoData(2, 3, "メモ内容");
    }

    @Test
    public void testTransmitHighlightMemo_whenRegisterSucceeds_returnsTrue() {
        try (MockedConstruction<RegisterHighlightMemo> mc =
                     Mockito.mockConstruction(RegisterHighlightMemo.class,
                         (mockReg, context) -> {
                             when(mockReg.registerHighlightMemo(
                                 eq(UID), eq(VOLUME_ID), eq(sampleData)
                             )).thenReturn(true);
                         })) {

            TransmitHighlightMemo tx =
                new TransmitHighlightMemo(mockContext, UID, VOLUME_ID);
            boolean result = tx.transmitHighlightMemo(sampleData);

            // ブラックボックス: true を返す
            assertTrue(result);

            // ホワイトボックス: registerHighlightMemo(...) が呼ばれている
            RegisterHighlightMemo constructed = mc.constructed().get(0);
            verify(constructed).registerHighlightMemo(UID, VOLUME_ID, sampleData);
        }
    }

    @Test
    public void testTransmitHighlightMemo_whenRegisterFails_returnsFalse() {
        try (MockedConstruction<RegisterHighlightMemo> mc =
                     Mockito.mockConstruction(RegisterHighlightMemo.class,
                         (mockReg, context) -> {
                             when(mockReg.registerHighlightMemo(
                                 eq(UID), eq(VOLUME_ID), eq(sampleData)
                             )).thenReturn(false);
                         })) {

            TransmitHighlightMemo tx =
                new TransmitHighlightMemo(mockContext, UID, VOLUME_ID);
            boolean result = tx.transmitHighlightMemo(sampleData);

            // ブラックボックス: false を返す
            assertFalse(result);

            // ホワイトボックス: registerHighlightMemo(...) 呼び出しを検証
            verify(mc.constructed().get(0))
                .registerHighlightMemo(UID, VOLUME_ID, sampleData);
        }
    }
}