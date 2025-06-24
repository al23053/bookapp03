package com.example.bookapp03.C5UserInformationManaging;

import android.app.Activity;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Logout の単体テスト
 * - ブラックボックス: true/false の戻り値を検証
 * - ホワイトボックス: FirebaseAuth.signOut() と startActivity 呼び出しを検証
 */
public class LogoutTest {

    private Activity mockActivity;
    private MockedStatic<FirebaseAuth> authStatic;
    private FirebaseAuth mockAuth;

    @Before
    public void setUp() {
        mockActivity = mock(Activity.class);
        mockAuth     = mock(FirebaseAuth.class);
        authStatic = Mockito.mockStatic(FirebaseAuth.class);
        authStatic.when(FirebaseAuth::getInstance).thenReturn(mockAuth);
    }

    @Test
    public void testLogout_success_returnsTrue() {
        Logout sut = new Logout(mockActivity);
        boolean result = sut.logout();
        assertTrue(result);
        verify(mockAuth).signOut();
        verify(mockActivity).startActivity(any(Intent.class));
    }

    @Test
    public void testLogout_exception_returnsFalse() {
        // signOut() で例外
        when(mockAuth.signOut()).thenThrow(new RuntimeException("fail"));
        Logout sut = new Logout(mockActivity);
        boolean result = sut.logout();
        assertFalse(result);
    }
}