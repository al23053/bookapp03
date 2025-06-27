package com.example.bookapp03.C2UserInformationProcessing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;

import com.example.bookapp03.C5UserInformationManaging.Logout;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * TransmitRequestLogoutの単体テスト
 * - ブラックボックス: 戻り値 true/false を検証
 * - ホワイトボックス: 内部で new Logout(...).logout() が呼ばれることを確認
 */
@RunWith(MockitoJUnitRunner.class)
public class TransmitRequestLogoutTest {

    @Mock
    private Activity mockActivity;

    @Before
    public void setUp() {
        // 標準的には activity.getApplicationContext() を挟むならスタブ化
        when(mockActivity.getApplicationContext()).thenReturn(mockActivity);
    }

    @Test
    public void testTransmitRequestLogout_whenLogoutSucceeds_returnsTrue() {
        try (MockedConstruction<Logout> mc = Mockito.mockConstruction(
                Logout.class,
                (mockLogout, context) -> when(mockLogout.logout()).thenReturn(true)
        )) {
            TransmitRequestLogout tx = new TransmitRequestLogout(mockActivity);
            boolean result = tx.transmitRequestLogout();

            // ブラックボックス
            assertTrue(result);
            // ホワイトボックス: logout() 呼び出し検証
            Logout constructed = mc.constructed().get(0);
            verify(constructed).logout();
        }
    }

    @Test
    public void testTransmitRequestLogout_whenLogoutFails_returnsFalse() {
        try (MockedConstruction<Logout> mc = Mockito.mockConstruction(
                Logout.class,
                (mockLogout, context) -> when(mockLogout.logout()).thenReturn(false)
        )) {
            TransmitRequestLogout tx = new TransmitRequestLogout(mockActivity);
            boolean result = tx.transmitRequestLogout();

            // ブラックボックス
            assertFalse(result);
            // ホワイトボックス
            Logout constructed = mc.constructed().get(0);
            verify(constructed).logout();
        }
    }
}