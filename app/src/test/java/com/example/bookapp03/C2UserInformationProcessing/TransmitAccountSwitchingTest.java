package com.example.bookapp03.C2UserInformationProcessing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;

import com.example.bookapp03.C5UserInformationManaging.RegisterAccountSwitching;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * TransmitAccountSwitchingの単体テスト
 * - ブラックボックス: メソッド戻り値 true/false を検証
 * - ホワイトボックス: 内部で new RegisterAccountSwitching(...).registerAccountSwitching(info) が呼ばれることを確認
 */
@RunWith(MockitoJUnitRunner.class)
public class TransmitAccountSwitchingTest {

    @Mock
    private Activity mockActivity;

    private UserInfo dummyInfo;

    @Before
    public void setUp() {
        // Activity#getApplicationContext() をそのまま返すスタブ
        when(mockActivity.getApplicationContext()).thenReturn(mockActivity);
        // テスト用 UserInfo
        dummyInfo = new UserInfo("uid123", "ニックネーム", "user@example.com");
    }

    @Test
    public void testTransmitAccountSwitching_whenRegisterSucceeds_returnsTrue() {
        try (MockedConstruction<RegisterAccountSwitching> mc =
                     Mockito.mockConstruction(RegisterAccountSwitching.class,
                         (mockReg, context) -> when(mockReg.registerAccountSwitching(dummyInfo)).thenReturn(true)
                     )) {
            TransmitAccountSwitching sut = new TransmitAccountSwitching(mockActivity);
            boolean result = sut.transmitAccountSwitching(dummyInfo);

            // ブラックボックス: true が返る
            assertTrue(result);
            // ホワイトボックス: registerAccountSwitching(info) が呼ばれている
            RegisterAccountSwitching constructed = mc.constructed().get(0);
            verify(constructed).registerAccountSwitching(dummyInfo);
        }
    }

    @Test
    public void testTransmitAccountSwitching_whenRegisterFails_returnsFalse() {
        try (MockedConstruction<RegisterAccountSwitching> mc =
                     Mockito.mockConstruction(RegisterAccountSwitching.class,
                         (mockReg, context) -> when(mockReg.registerAccountSwitching(dummyInfo)).thenReturn(false)
                     )) {
            TransmitAccountSwitching sut = new TransmitAccountSwitching(mockActivity);
            boolean result = sut.transmitAccountSwitching(dummyInfo);

            // ブラックボックス: false が返る
            assertFalse(result);
            // ホワイトボックス: registerAccountSwitching(info) が呼ばれている
            RegisterAccountSwitching constructed = mc.constructed().get(0);
            verify(constructed).registerAccountSwitching(dummyInfo);
        }
    }
}