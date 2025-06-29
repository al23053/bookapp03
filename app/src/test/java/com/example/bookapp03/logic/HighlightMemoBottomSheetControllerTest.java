package com.example.bookapp03.logic;

import android.view.View;
import android.widget.TextView;

import com.example.bookapp03.R; // R.id.highlight_memo_text を参照するため必要

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class HighlightMemoBottomSheetControllerTest {

    private HighlightMemoBottomSheetController controller;

    @Mock
    private View mockRootView;
    @Mock
    private TextView mockMemoTextView;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new HighlightMemoBottomSheetController();
    }

    /**
     * displayMemo: rootViewがnullの場合、何も処理しないことをテスト
     * 命令網羅
     */
    @Test
    public void displayMemo_whenRootViewIsNull_doesNothing() {
        // When
        controller.displayMemo(null, "Test Memo");

        // Then
        // mockRootView のメソッドが何も呼び出されていないことを検証 (findViewByIdなど)
        verifyNoInteractions(mockRootView);
        verifyNoInteractions(mockMemoTextView);
    }

    /**
     * displayMemo: TextViewが存在し、メモテキストがnullでない場合に正しく表示されることをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void displayMemo_whenTextViewExistsAndMemoIsNotNull_setsTextCorrectly() {
        // Given
        String testMemo = "これはテストメモです。";
        // mockRootView.findViewById(R.id.highlight_memo_text) が mockMemoTextView を返すように設定
        when(mockRootView.findViewById(R.id.highlight_memo_text)).thenReturn(mockMemoTextView);

        // When
        controller.displayMemo(mockRootView, testMemo);

        // Then
        // findViewById が正しいIDで呼び出されたことを検証
        verify(mockRootView).findViewById(R.id.highlight_memo_text);
        // setText が正しいメモテキストで呼び出されたことを検証
        verify(mockMemoTextView).setText(testMemo);
    }

    /**
     * displayMemo: TextViewが存在し、メモテキストがnullの場合に空文字列が表示されることをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void displayMemo_whenTextViewExistsAndMemoIsNull_setsEmptyString() {
        // Given
        // mockRootView.findViewById(R.id.highlight_memo_text) が mockMemoTextView を返すように設定
        when(mockRootView.findViewById(R.id.highlight_memo_text)).thenReturn(mockMemoTextView);

        // When
        controller.displayMemo(mockRootView, null);

        // Then
        // findViewById が正しいIDで呼び出されたことを検証
        verify(mockRootView).findViewById(R.id.highlight_memo_text);
        // setText が空文字列で呼び出されたことを検証
        verify(mockMemoTextView).setText("");
    }

    /**
     * displayMemo: TextViewが存在しない場合、何も処理しないことをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void displayMemo_whenTextViewDoesNotExist_doesNothing() {
        // Given
        // mockRootView.findViewById(R.id.highlight_memo_text) が null を返すように設定
        when(mockRootView.findViewById(R.id.highlight_memo_text)).thenReturn(null);

        // When
        controller.displayMemo(mockRootView, "Test Memo");

        // Then
        // findViewById が呼び出されたことは検証するが、それ以外の TextView のメソッドは呼び出されないことを検証
        verify(mockRootView).findViewById(R.id.highlight_memo_text);
        verifyNoInteractions(mockMemoTextView); // mockMemoTextView のsetTextなど、他のメソッドが呼び出されないことを確認
    }
}