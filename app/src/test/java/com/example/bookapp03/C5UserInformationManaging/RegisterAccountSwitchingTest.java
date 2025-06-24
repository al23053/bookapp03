package com.example.bookapp03.C5UserInformationManaging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.bookapp03.C2UserInformationProcessing.UserInfo;

import org.junit.Before;
import org.junit.Test;

/**
 * RegisterAccountSwitching の単体テスト
 * - ブラックボックス: true/false の戻り値を検証
 * - ホワイトボックス: SharedPreferences.Editor の呼び出しを検証
 */
public class RegisterAccountSwitchingTest {

    private Activity mockActivity;
    private Context mockContext;
    private SharedPreferences mockPrefs;
    private SharedPreferences.Editor mockEditor;
    private RegisterAccountSwitching sut;
    private UserInfo dummyInfo;

    @Before
    public void setUp() {
        mockActivity = mock(Activity.class);
        mockContext  = mock(Context.class);
        mockPrefs    = mock(SharedPreferences.class);
        mockEditor   = mock(SharedPreferences.Editor.class);

        when(mockActivity.getApplicationContext()).thenReturn(mockContext);
        when(mockContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putString("current_uid", "u1")).thenReturn(mockEditor);
        when(mockEditor.putString("current_nickname", "nick")).thenReturn(mockEditor);
        when(mockEditor.putString("current_email", "e@x")).thenReturn(mockEditor);

        sut = new RegisterAccountSwitching(mockActivity);
        dummyInfo = new UserInfo("u1", "nick", "e@x");
    }

    @Test
    public void testRegisterAccountSwitching_success_returnsTrue() {
        boolean result = sut.registerAccountSwitching(dummyInfo);
        assertTrue(result);
        verify(mockEditor).putString("current_uid", "u1");
        verify(mockEditor).putString("current_nickname", "nick");
        verify(mockEditor).putString("current_email", "e@x");
        verify(mockEditor).apply();
    }

    @Test
    public void testRegisterAccountSwitching_exception_returnsFalse() {
        // 編集時に例外を投げる
        when(mockPrefs.edit()).thenThrow(new RuntimeException("err"));
        sut = new RegisterAccountSwitching(mockActivity);
        boolean result = sut.registerAccountSwitching(dummyInfo);
        assertFalse(result);
    }
}