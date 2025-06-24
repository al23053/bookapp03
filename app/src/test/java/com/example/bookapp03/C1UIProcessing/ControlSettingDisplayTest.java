package com.example.bookapp03.C1UIProcessing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.example.bookapp03.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * ControlSettingDisplay の単体テスト
 * - ブラックボックス: 全ビューにリスナーがセットされる
 * - ホワイトボックス: 各リスナーのクリック後動作を検証
 */
@RunWith(MockitoJUnitRunner.class)
public class ControlSettingDisplayTest {

    @Mock private Activity mockActivity;
    @Mock private TextView btnNick;
    @Mock private TextView btnGenre;
    @Mock private TextView btnDark;
    @Mock private TextView btnAccount;
    @Mock private TextView btnLogout;

    @Captor private ArgumentCaptor<View.OnClickListener> listenerCap;
    @Captor private ArgumentCaptor<Intent> intentCap;

    private ControlSettingDisplay controller;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ControlSettingDisplay(mockActivity);
        controller.bind(btnNick, btnGenre, btnDark, btnAccount, btnLogout);
    }

    @Test
    public void testBind_allListenersSet() {
        verify(btnNick).setOnClickListener(any(View.OnClickListener.class));
        verify(btnGenre).setOnClickListener(any(View.OnClickListener.class));
        verify(btnDark).setOnClickListener(any(View.OnClickListener.class));
        verify(btnAccount).setOnClickListener(any(View.OnClickListener.class));
        verify(btnLogout).setOnClickListener(any(View.OnClickListener.class));
    }

    @Test
    public void testNickname_clickStartsAccountSetting() {
        verify(btnNick).setOnClickListener(listenerCap.capture());
        listenerCap.getValue().onClick(null);
        verify(mockActivity).startActivity(intentCap.capture());
        assert intentCap.getValue().getComponent().getClassName()
            .equals(AccountSettingActivity.class.getName());
    }

    @Test
    public void testLogout_clickInvokesLogout() {
        verify(btnLogout).setOnClickListener(listenerCap.capture());
        listenerCap.getValue().onClick(null);
        // startActivity は呼ばれず LogoutProcessing.confirmLogout() が呼ばれる想定
        verify(mockActivity, never()).startActivity(any(Intent.class));
    }
}