package com.example.bookapp03.C1UIProcessing;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatDelegate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * ControlDarkmodeSwitch の単体テスト
 * – ブラックボックス: bind() で Switch#setChecked が呼ばれる
 * – ホワイトボックス: チェック変更時の SharedPreferences／recreate 呼び出し検証
 */
@RunWith(MockitoJUnitRunner.class)
public class ControlDarkmodeSwitchTest {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_DARK    = "dark_mode";

    @Mock
    private Activity mockActivity;
    @Mock
    private Switch mockSwitch;
    @Mock
    private SharedPreferences mockPrefs;
    @Mock
    private SharedPreferences.Editor mockEditor;
    @Captor
    private ArgumentCaptor<CompoundButton.OnCheckedChangeListener> listenerCaptor;

    @Before
    public void setUp() {
        when(mockActivity.getSharedPreferences(eq(PREFS_NAME), eq(Context.MODE_PRIVATE)))
            .thenReturn(mockPrefs);
        when(mockPrefs.getBoolean(eq(KEY_DARK), eq(false))).thenReturn(true);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putBoolean(eq(KEY_DARK), eq(false))).thenReturn(mockEditor);

        ControlDarkmodeSwitch controller = new ControlDarkmodeSwitch(mockActivity, mockSwitch);
        controller.bind();
    }

    @Test
    public void testBind_initializesSwitchChecked() {
        verify(mockSwitch).setChecked(true);
    }

    @Test
    public void testOnCheckedChanged_writesPrefsAndRecreate() {
        verify(mockSwitch).setOnCheckedChangeListener(listenerCaptor.capture());
        CompoundButton.OnCheckedChangeListener listener = listenerCaptor.getValue();

        // OFF に切り替えをシミュレート
        listener.onCheckedChanged(mockSwitch, false);

        verify(mockEditor).putBoolean(KEY_DARK, false);
        verify(mockEditor).apply();
        verify(mockActivity).recreate();
    }
}