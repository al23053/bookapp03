package com.example.bookapp03.logic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.DialogInterface;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.Shadows; // Shadowsクラスをインポート

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

// RobolectricTestRunner を使用してAndroid環境をシミュレート
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29) // テスト対象のSDKバージョンを指定 (API 29 は Android 10)
public class TutorialManagerTest {

    @Mock
    private Context mockContext;
    @Mock
    private SharedPreferences mockPrefs;
    @Mock
    private SharedPreferences.Editor mockEditor;
    @Mock
    private Activity mockActivity;
    @Mock
    private Runnable mockOnCompleteCallback;

    private TutorialManager tutorialManager;

    private static final String PREF_NAME = "tutorial_pref";
    private static final String KEY_SHOWN = "tutorial_shown";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(mockContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor);

        // voidメソッドのモックには doAnswer().when() を使用
        doAnswer(invocation -> {
            Boolean value = invocation.getArgument(1); // putBooleanの第2引数 (value)
            // SharedPreferencesの状態をモックで表現 (getBooleanの戻り値を更新)
            when(mockPrefs.getBoolean(KEY_SHOWN, false)).thenReturn(value);
            return null; // voidメソッドなのでnullを返す
        }).when(mockEditor).apply();

        tutorialManager = new TutorialManager(mockContext);
    }

    @Test
    public void shouldShowTutorial_returnsTrue_whenNotShown() {
        when(mockPrefs.getBoolean(KEY_SHOWN, false)).thenReturn(false);
        assertTrue("チュートリアルが表示されていない場合、trueを返すこと", tutorialManager.shouldShowTutorial());
        verify(mockPrefs).getBoolean(KEY_SHOWN, false);
    }

    @Test
    public void shouldShowTutorial_returnsFalse_whenAlreadyShown() {
        when(mockPrefs.getBoolean(KEY_SHOWN, false)).thenReturn(true);
        assertFalse("チュートリアルが既に表示されている場合、falseを返すこと", tutorialManager.shouldShowTutorial());
        verify(mockPrefs).getBoolean(KEY_SHOWN, false);
    }

    @Test
    public void markTutorialAsShown_updatesSharedPreferences() {
        tutorialManager.markTutorialAsShown();
        verify(mockEditor).putBoolean(KEY_SHOWN, true);
        verify(mockEditor).apply();
    }

    @Test
    public void showTutorialIfNeeded_showsDialogAndMarksShown_whenNotShownAndOKClicked() {
        when(mockPrefs.getBoolean(KEY_SHOWN, false)).thenReturn(false); // チュートリアルはまだ表示されていない

        try (MockedStatic<AlertDialog.Builder> mockedBuilder = mockStatic(AlertDialog.Builder.class)) {
            AlertDialog.Builder builder = mock(AlertDialog.Builder.class);

            when(builder.setTitle(anyString())).thenReturn(builder);
            when(builder.setMessage(anyString())).thenReturn(builder);
            when(builder.setPositiveButton(anyString(), any(DialogInterface.OnClickListener.class))).thenReturn(builder);
            when(builder.setNegativeButton(anyString(), any(DialogInterface.OnClickListener.class))).thenReturn(builder);

            mockedBuilder.when(() -> new AlertDialog.Builder(mockActivity)).thenReturn(builder);

            // When
            tutorialManager.showTutorialIfNeeded(mockActivity, mockOnCompleteCallback);

            // Then
            verify(builder).setTitle("使い方");
            verify(builder).setMessage("ここで使い方を説明する");
            verify(builder).setPositiveButton(eq("OK"), any(DialogInterface.OnClickListener.class));
            verify(builder).setNegativeButton(eq("スキップ"), any(DialogInterface.OnClickListener.class));
            verify(builder).show(); // show()が呼ばれたことを検証

            // Robolectricから表示されたダイアログのシャドウを取得します。
            // AlertDialog.getLatestAlertDialog() は android.app.AlertDialog を返しますが、
            // テスト用のメソッド (clickPositiveButtonなど) は ShadowAlertDialog にあるため、
            // Shadows.shadowOf() を使って ShadowAlertDialog にキャストします。
            AlertDialog actualAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
            assertNotNull("ダイアログが表示されていること", actualAlertDialog);
            ShadowAlertDialog shadowAlertDialog = Shadows.shadowOf(actualAlertDialog);


            // OKボタンのクリックをシミュレート
            shadowAlertDialog.clickPositiveButton();

            // markTutorialAsShown()が呼び出されたことを検証
            verify(mockEditor).putBoolean(KEY_SHOWN, true);
            verify(mockEditor).apply();
            // onCompleteコールバックが呼び出されたことを検証
            verify(mockOnCompleteCallback, times(1)).run();
        }
    }

    @Test
    public void showTutorialIfNeeded_showsDialogAndDoesNotMarkShown_whenNotShownAndSkipClicked() {
        when(mockPrefs.getBoolean(KEY_SHOWN, false)).thenReturn(false); // チュートリアルはまだ表示されていない

        try (MockedStatic<AlertDialog.Builder> mockedBuilder = mockStatic(AlertDialog.Builder.class)) {
            AlertDialog.Builder builder = mock(AlertDialog.Builder.class);
            when(builder.setTitle(anyString())).thenReturn(builder);
            when(builder.setMessage(anyString())).thenReturn(builder);
            when(builder.setPositiveButton(anyString(), any(DialogInterface.OnClickListener.class))).thenReturn(builder);
            when(builder.setNegativeButton(anyString(), any(DialogInterface.OnClickListener.class))).thenReturn(builder);
            mockedBuilder.when(() -> new AlertDialog.Builder(mockActivity)).thenReturn(builder);

            tutorialManager.showTutorialIfNeeded(mockActivity, mockOnCompleteCallback);

            verify(builder).setTitle("使い方");
            verify(builder).setMessage("ここで使い方を説明する");
            verify(builder).setPositiveButton(eq("OK"), any(DialogInterface.OnClickListener.class));
            verify(builder).setNegativeButton(eq("スキップ"), any(DialogInterface.OnClickListener.class));
            verify(builder).show();

            AlertDialog actualAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
            assertNotNull("ダイアログが表示されていること", actualAlertDialog);
            ShadowAlertDialog shadowAlertDialog = Shadows.shadowOf(actualAlertDialog);

            // スキップボタンのクリックをシミュレート
            shadowAlertDialog.clickNegativeButton();

            // markTutorialAsShown()が呼び出されていないことを検証
            verify(mockEditor, never()).putBoolean(eq(KEY_SHOWN), eq(true));
            verify(mockEditor, never()).apply();
            // onCompleteコールバックが呼び出されたことを検証
            verify(mockOnCompleteCallback, times(1)).run();
        }
    }

    @Test
    public void showTutorialIfNeeded_doesNotShowDialog_whenAlreadyShown() {
        when(mockPrefs.getBoolean(KEY_SHOWN, false)).thenReturn(true); // チュートリアルは既に表示済み

        try (MockedStatic<AlertDialog.Builder> mockedBuilder = mockStatic(AlertDialog.Builder.class)) {
            tutorialManager.showTutorialIfNeeded(mockActivity, mockOnCompleteCallback);

            mockedBuilder.verifyNoInteractions(); // AlertDialog.Builderのメソッドが何も呼び出されていないことを検証
            verify(mockOnCompleteCallback, times(1)).run(); // onCompleteコールバックが呼び出されたことを検証
            verify(mockEditor, never()).putBoolean(anyString(), anyBoolean());
            verify(mockEditor, never()).apply();
        }
    }
}