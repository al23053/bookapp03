package com.example.bookapp03.C5UserInformationManaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * UserAuthManagerの単体テスト
 * - ブラックボックス: getCurrentUid が空文字／UID を返すこと
 * - ホワイトボックス: FirebaseAuth.getInstance(), getCurrentUser() の呼び出し検証
 */
public class UserAuthManagerTest {

    private MockedStatic<FirebaseAuth> authStatic;
    private FirebaseAuth mockAuth;
    private FirebaseUser mockUser;

    @Before
    public void setUp() {
        mockAuth = mock(FirebaseAuth.class);
        authStatic = Mockito.mockStatic(FirebaseAuth.class);
        authStatic.when(FirebaseAuth::getInstance).thenReturn(mockAuth);
        mockUser = mock(FirebaseUser.class);
    }

    @After
    public void tearDown() {
        authStatic.close();
    }

    @Test
    public void testGetCurrentUid_userIsNull_returnsEmpty() {
        when(mockAuth.getCurrentUser()).thenReturn(null);
        String uid = UserAuthManager.getCurrentUid();
        assertEquals("", uid);
    }

    @Test
    public void testGetCurrentUid_userExists_returnsUid() {
        when(mockUser.getUid()).thenReturn("abc123");
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        String uid = UserAuthManager.getCurrentUid();
        assertEquals("abc123", uid);
    }
}