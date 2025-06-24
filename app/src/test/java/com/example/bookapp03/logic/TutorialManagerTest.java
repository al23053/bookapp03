package com.example.bookapp03.logic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.app.Activity;
import android.app.AlertDialog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor; // AlertDialogのビルダーをキャプチャするため

@ExtendWith(MockitoExtension.class)
public class TutorialManagerTest {

    @Mock
    private Context mockContext;
    @Mock
    private SharedPreferences mockPrefs;
    @Mock
    private SharedPreferences.Editor mockEditor;
    @Mock
    private Activity mockActivity; // showTutorialIfNeeded のテスト用
    @Mock
    private AlertDialog.Builder mockAlertDialogBuilder; // AlertDialogのモック用
    @Mock
    private AlertDialog mockAlertDialog; // AlertDialogのモック用

    private TutorialManager tutorialManager;

    @BeforeEach
    void setUp() {
        // Context.getSharedPreferences() が mockPrefs を返すように設定
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        // SharedPreferences.edit() が mockEditor を返すように設定
        when(mockPrefs.edit()).thenReturn(mockEditor);
        // SharedPreferences.Editor の putBoolean と apply をチェイン可能にする
        when(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor);

        tutorialManager = new TutorialManager(mockContext);
    }

    @Test
    void testShouldShowTutorial_true() {
        // KEY_SHOWN が false の場合 (まだ表示されていない場合)
        when(mockPrefs.getBoolean("tutorial_shown", false)).thenReturn(false);
        assertTrue(tutorialManager.shouldShowTutorial());
    }

    @Test
    void testShouldShowTutorial_false() {
        // KEY_SHOWN が true の場合 (すでに表示されている場合)
        when(mockPrefs.getBoolean("tutorial_shown", false)).thenReturn(true);
        assertFalse(tutorialManager.shouldShowTutorial());
    }

    @Test
    void testMarkTutorialAsShown() {
        tutorialManager.markTutorialAsShown();
        // putBoolean が KEY_SHOWN と true で呼ばれ、apply が呼ばれたことを検証
        verify(mockEditor).putBoolean("tutorial_shown", true);
        verify(mockEditor).apply();
    }

    @Test
    void testShowTutorialIfNeeded_showsTutorial() {
        // チュートリアルが表示されるべき場合
        when(mockPrefs.getBoolean("tutorial_shown", false)).thenReturn(false);

        // AlertDialog.Builder をモック化
        when(mockActivity.getApplicationContext()).thenReturn(mockContext); // BuilderのコンストラクタがContextを使うため
        try (MockedStatic<AlertDialog.Builder> mockedBuilder = mockStatic(AlertDialog.Builder.class)) {
            mockedBuilder.when(() -> new AlertDialog.Builder(mockActivity)).thenReturn(mockAlertDialogBuilder);
            when(mockAlertDialogBuilder.setTitle(anyString())).thenReturn(mockAlertDialogBuilder);
            when(mockAlertDialogBuilder.setMessage(anyString())).thenReturn(mockAlertDialogBuilder);
            when(mockAlertDialogBuilder.setPositiveButton(anyString(), any())).thenReturn(mockAlertDialogBuilder);
            when(mockAlertDialogBuilder.setNegativeButton(anyString(), any())).thenReturn(mockAlertDialogBuilder);
            when(mockAlertDialogBuilder.show()).thenReturn(mockAlertDialog); // show()が呼ばれてAlertDialogが返る

            Runnable mockOnComplete = mock(Runnable.class);
            tutorialManager.showTutorialIfNeeded(mockActivity, mockOnComplete);

            // AlertDialog.Builder が作成され、show() が呼ばれたことを検証
            mockedBuilder.verify(() -> new AlertDialog.Builder(mockActivity));
            verify(mockAlertDialogBuilder).show();

            // 正のボタンクリックの引数をキャプチャし、実行してみる
            ArgumentCaptor<android.content.DialogInterface.OnClickListener> positiveClickListenerCaptor =
                    ArgumentCaptor.forClass(android.content.DialogInterface.OnClickListener.class);
            verify(mockAlertDialogBuilder).setPositiveButton(anyString(), positiveClickListenerCaptor.capture());

            // キャプチャしたリスナーを実行 (ダイアログが閉じられたことをシミュレート)
            positiveClickListenerCaptor.getValue().onClick(mock(android.content.DialogInterface.class), 0);

            // チュートリアルが表示済みになり、onComplete が実行されることを検証
            verify(mockEditor).putBoolean("tutorial_shown", true);
            verify(mockEditor).apply();
            verify(mockOnComplete).run();
        }
    }

    @Test
    void testShowTutorialIfNeeded_skipsTutorial() {
        // チュートリアルがすでに表示されている場合
        when(mockPrefs.getBoolean("tutorial_shown", false)).thenReturn(true);

        Runnable mockOnComplete = mock(Runnable.class);
        tutorialManager.showTutorialIfNeeded(mockActivity, mockOnComplete);

        // AlertDialog.Builder は作成されないことを検証
        try (MockedStatic<AlertDialog.Builder> mockedBuilder = mockStatic(AlertDialog.Builder.class)) {
            mockedBuilder.verifyNoInteractions();
        }

        // チュートリアルが表示済みにマークされないことを検証
        verify(mockEditor, never()).putBoolean(anyString(), anyBoolean());
        verify(mockEditor, never()).apply();

        // onComplete が直接実行されることを検証
        verify(mockOnComplete).run();
    }
}