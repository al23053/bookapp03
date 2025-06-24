package com.example.bookapp03.C3BookInformationProcessing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.example.bookapp03.C6BookInformationManaging.RegisterSummary;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * TransmitSummary の単体テスト
 * - ブラックボックス: メソッド戻り値 (true/false) を検証
 * - ホワイトボックス: RegisterSummary#registerSummary が内部で呼ばれることを確認
 */
@RunWith(MockitoJUnitRunner.class)
public class TransmitSummaryTest {

    @Mock
    private Context mockContext;

    private static final String UID          = "u1";
    private static final String VOLUME_ID    = "v1";
    private static final String SUMMARY_TEXT = "テスト要約";
    private static final boolean IS_PUBLIC   = true;

    @Before
    public void setUp() {
        // ApplicationContext 取得をスタブ化
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
    }

    @Test
    public void testTransmitSummary_whenRegisterSucceeds_returnsTrue() {
        try (MockedConstruction<RegisterSummary> mc =
                     Mockito.mockConstruction(RegisterSummary.class,
                         (mockReg, context) -> {
                             when(mockReg.registerSummary(
                                 eq(UID),
                                 eq(VOLUME_ID),
                                 eq(SUMMARY_TEXT),
                                 eq(IS_PUBLIC)
                             )).thenReturn(true);
                         })) {

            TransmitSummary sut = new TransmitSummary(mockContext);
            boolean result = sut.transmitSummary(UID, VOLUME_ID, SUMMARY_TEXT, IS_PUBLIC);

            // ブラックボックス: true が返る
            assertTrue(result);

            // ホワイトボックス: registerSummary(...) が１回呼ばれている
            RegisterSummary constructed = mc.constructed().get(0);
            verify(constructed).registerSummary(UID, VOLUME_ID, SUMMARY_TEXT, IS_PUBLIC);
        }
    }

    @Test
    public void testTransmitSummary_whenRegisterFails_returnsFalse() {
        try (MockedConstruction<RegisterSummary> mc =
                     Mockito.mockConstruction(RegisterSummary.class,
                         (mockReg, context) -> {
                             when(mockReg.registerSummary(
                                 anyString(),
                                 anyString(),
                                 anyString(),
                                 anyBoolean()
                             )).thenReturn(false);
                         })) {

            TransmitSummary sut = new TransmitSummary(mockContext);
            boolean result = sut.transmitSummary(UID, VOLUME_ID, SUMMARY_TEXT, IS_PUBLIC);

            // ブラックボックス: false が返る
            assertFalse(result);

            // ホワイトボックス: registerSummary(...) 呼び出しを検証
            RegisterSummary constructed = mc.constructed().get(0);
            verify(constructed).registerSummary(anyString(), anyString(), anyString(), anyBoolean());
        }
    }
}