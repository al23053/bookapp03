package com.example.bookapp03.logic;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp03.R;
import com.example.bookapp03.data.model.BookSummaryData;
import com.example.bookapp03.ui.BookListAdapter;
import com.example.bookapp03.ui.PublicPrivateToggleHandler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class BookListViewControllerTest {

    private BookListViewController controller;

    @Mock
    private RecyclerView mockRecyclerView;
    @Mock
    private Context mockContext;
    @Mock
    private View mockParentView; // recyclerView.getParent() が返すView
    @Mock
    private TextView mockEmptyTextView;
    @Mock
    private PublicPrivateToggleHandler mockToggleHandler;

    // StaticメソッドをモックするためのMockedStatic
    private MockedStatic<Log> mockedLog;
    private MockedStatic<GridLayoutManager> mockedGridLayoutManager;
    private MockedStatic<BookListAdapter> mockedBookListAdapter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new BookListViewController();

        // RecyclerView.getContext() のモック
        when(mockRecyclerView.getContext()).thenReturn(mockContext);

        // RecyclerView.getParent() のモック
        when(mockRecyclerView.getParent()).thenReturn(mockParentView);

        // parentView.findViewById(R.id.empty_text) のモック
        when(mockParentView.findViewById(R.id.empty_text)).thenReturn(mockEmptyTextView);

        // Log.e のモック
        mockedLog = mockStatic(Log.class);
        when(Log.e(anyString(), anyString())).thenReturn(0);

        // GridLayoutManager のコンストラクタをモック
        mockedGridLayoutManager = mockStatic(GridLayoutManager.class);
        // GridLayoutManager のインスタンスが作成されると、このモックインスタンスが返される
        when(new GridLayoutManager(any(Context.class), anyInt())).thenReturn(mock(GridLayoutManager.class));

        // BookListAdapter のコンストラクタをモック
        mockedBookListAdapter = mockStatic(BookListAdapter.class);
        // BookListAdapter のインスタンスが作成されると、このモックインスタンスが返される
        when(new BookListAdapter(anyList(), any(PublicPrivateToggleHandler.class))).thenReturn(mock(BookListAdapter.class));
    }

    @After
    public void tearDown() {
        // MockedStatic をクローズすることを忘れない
        if (mockedLog != null) {
            mockedLog.close();
        }
        if (mockedGridLayoutManager != null) {
            mockedGridLayoutManager.close();
        }
        if (mockedBookListAdapter != null) {
            mockedBookListAdapter.close();
        }
    }

    /**
     * displayBookList: recyclerViewがnullの場合、エラーログが出力され、何も処理しないことをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void displayBookList_recyclerViewIsNull_logsErrorAndDoesNothing() {
        // Given
        List<BookSummaryData> emptyList = new ArrayList<>();

        // When
        controller.displayBookList(null, emptyList, mockToggleHandler, true);

        // Then
        // Log.e が呼ばれることを検証
        mockedLog.verify(() -> Log.e(eq("BookListViewController"), eq("RecyclerViewがnull")), times(1));

        // 他のメソッドは何も呼び出されないことを検証
        verifyNoInteractions(mockRecyclerView);
        verifyNoInteractions(mockParentView);
        verifyNoInteractions(mockEmptyTextView);
        mockedGridLayoutManager.verifyNoInteractions(); // GridLayoutManagerのコンストラクタも呼ばれない
        mockedBookListAdapter.verifyNoInteractions(); // BookListAdapterのコンストラクタも呼ばれない
    }

    /**
     * displayBookList: 書籍リストが空で、空メッセージ表示が有効な場合
     * レイアウトとアダプターが設定され、空メッセージが表示されることをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void displayBookList_emptyListAndShowMessageTrue_setsAdapterAndShowsEmptyMessage() {
        // Given
        List<BookSummaryData> emptyList = new ArrayList<>();
        boolean showEmptyMessage = true;

        // When
        controller.displayBookList(mockRecyclerView, emptyList, mockToggleHandler, showEmptyMessage);

        // Then
        // レイアウトマネージャーが設定されることを検証
        verify(mockRecyclerView).setLayoutManager(any(GridLayoutManager.class));
        mockedGridLayoutManager.verify(() -> new GridLayoutManager(eq(mockContext), eq(3)));

        // アダプターが設定されることを検証
        verify(mockRecyclerView).setAdapter(any(BookListAdapter.class));
        mockedBookListAdapter.verify(() -> new BookListAdapter(eq(emptyList), eq(mockToggleHandler)));

        // 空メッセージViewが見つかり、可視化され、テキストが設定されることを検証
        verify(mockRecyclerView).getParent(); // parentView取得
        verify(mockParentView).findViewById(R.id.empty_text);
        verify(mockEmptyTextView).setVisibility(View.VISIBLE);
        verify(mockEmptyTextView).setText(R.string.no_book_message);
    }

    /**
     * displayBookList: 書籍リストが空で、空メッセージ表示が無効な場合
     * レイアウトとアダプターが設定され、空メッセージは非表示になることをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void displayBookList_emptyListAndShowMessageFalse_setsAdapterAndHidesEmptyMessage() {
        // Given
        List<BookSummaryData> emptyList = new ArrayList<>();
        boolean showEmptyMessage = false;

        // When
        controller.displayBookList(mockRecyclerView, emptyList, mockToggleHandler, showEmptyMessage);

        // Then
        verify(mockRecyclerView).setLayoutManager(any(GridLayoutManager.class));
        verify(mockRecyclerView).setAdapter(any(BookListAdapter.class));

        // 空メッセージViewが見つかり、非可視化されることを検証
        verify(mockRecyclerView).getParent();
        verify(mockParentView).findViewById(R.id.empty_text);
        verify(mockEmptyTextView).setVisibility(View.GONE); // 空リストだが非表示
        verify(mockEmptyTextView, never()).setText(anyInt()); // テキストは設定されない
    }

    /**
     * displayBookList: 書籍リストが空でなく、空メッセージ表示が有効な場合
     * レイアウトとアダプターが設定され、空メッセージは非表示になることをテスト (常に非表示になるはず)
     * 命令網羅、分岐網羅
     */
    @Test
    public void displayBookList_nonEmptyListAndShowMessageTrue_setsAdapterAndHidesEmptyMessage() {
        // Given
        List<BookSummaryData> nonEmptyList = Arrays.asList(
                new BookSummaryData("id1", "Book1", "url1"),
                new BookSummaryData("id2", "Book2", "url2")
        );
        boolean showEmptyMessage = true; // 有効でもリストが空でないので非表示になるはず

        // When
        controller.displayBookList(mockRecyclerView, nonEmptyList, mockToggleHandler, showEmptyMessage);

        // Then
        verify(mockRecyclerView).setLayoutManager(any(GridLayoutManager.class));
        verify(mockRecyclerView).setAdapter(any(BookListAdapter.class));

        // 空メッセージViewが見つかり、非可視化されることを検証
        verify(mockRecyclerView).getParent();
        verify(mockParentView).findViewById(R.id.empty_text);
        verify(mockEmptyTextView).setVisibility(View.GONE); // リストが空ではないので非表示
        verify(mockEmptyTextView, never()).setText(anyInt());
    }

    /**
     * displayBookList: 書籍リストが空でなく、空メッセージ表示が無効な場合
     * レイアウトとアダプターが設定され、空メッセージは非表示になることをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void displayBookList_nonEmptyListAndShowMessageFalse_setsAdapterAndHidesEmptyMessage() {
        // Given
        List<BookSummaryData> nonEmptyList = Arrays.asList(
                new BookSummaryData("id1", "Book1", "url1"),
                new BookSummaryData("id2", "Book2", "url2")
        );
        boolean showEmptyMessage = false;

        // When
        controller.displayBookList(mockRecyclerView, nonEmptyList, mockToggleHandler, showEmptyMessage);

        // Then
        verify(mockRecyclerView).setLayoutManager(any(GridLayoutManager.class));
        verify(mockRecyclerView).setAdapter(any(BookListAdapter.class));

        // 空メッセージViewが見つかり、非可視化されることを検証
        verify(mockRecyclerView).getParent();
        verify(mockParentView).findViewById(R.id.empty_text);
        verify(mockEmptyTextView).setVisibility(View.GONE); // リストが空ではないので非表示
        verify(mockEmptyTextView, never()).setText(anyInt());
    }

    /**
     * displayBookList: empty_text TextViewが見つからない場合 (findViewByIdがnullを返す場合)
     * 例外が発生せず、他の処理は継続されることをテスト
     * 命令網羅
     */
    @Test
    public void displayBookList_emptyTextViewIsNull_noExceptionsAndContinues() {
        // Given
        List<BookSummaryData> nonEmptyList = Arrays.asList(
                new BookSummaryData("id1", "Book1", "url1")
        );
        boolean showEmptyMessage = true;
        // findViewById(R.id.empty_text) が null を返すように設定
        when(mockParentView.findViewById(R.id.empty_text)).thenReturn(null);

        // When
        controller.displayBookList(mockRecyclerView, nonEmptyList, mockToggleHandler, showEmptyMessage);

        // Then
        // 例外が発生しないことを確認 (テストが成功すればOK)
        // レイアウトマネージャーとアダプターは設定されることを検証
        verify(mockRecyclerView).setLayoutManager(any(GridLayoutManager.class));
        verify(mockRecyclerView).setAdapter(any(BookListAdapter.class));

        // mockEmptyTextView には何もインタラクションがないことを検証
        verifyNoInteractions(mockEmptyTextView);
    }
}