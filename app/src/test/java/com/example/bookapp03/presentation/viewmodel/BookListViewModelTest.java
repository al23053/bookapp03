package com.example.bookapp03.presentation.viewmodel;

import android.util.Log;

import com.example.bookapp03.C3BookInformationProcessing.BookListViewModel;
import com.example.bookapp03.C3BookInformationProcessing.BookSummaryData;
import com.example.bookapp03.C6BookInformationManaging.BookRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class BookListViewModelTest {

    private BookListViewModel viewModel;

    @Mock
    private BookRepository mockBookRepository;
    @Mock
    private Future<List<BookSummaryData>> mockBooksFuture;
    @Mock
    private Future<Boolean> mockUpdatePublicStatusFuture;

    // Log.e と Log.d をモックするためのMockedStatic
    private MockedStatic<Log> mockedLog;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        viewModel = new BookListViewModel(mockBookRepository);

        // Logのモック設定
        mockedLog = mockStatic(Log.class);
        when(Log.e(anyString(), anyString())).thenReturn(0);
        when(Log.d(anyString(), anyString())).thenReturn(0);
    }

    @After
    public void tearDown() {
        if (mockedLog != null) {
            mockedLog.close();
        }
    }

    /**
     * loadBooks: 書籍リストの取得が成功した場合
     * _bookList LiveDataが正しいデータで更新されることをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void loadBooks_success_postsBookSummaryList() throws Exception {
        // Given
        List<BookSummaryData> testList = Arrays.asList(
                new BookSummaryData("id1", "Title 1", "url1"),
                new BookSummaryData("id2", "Title 2", "url2")
        );
        when(mockBookRepository.getAllBooks(anyString())).thenReturn(mockBooksFuture);
        when(mockBooksFuture.get()).thenReturn(testList);

        // When
        viewModel.loadBooks("uid");

        // Then
        verify(mockBookRepository).getAllBooks(eq("uid"));
        Thread.sleep(100); // 非同期処理完了を待機
        assertEquals(testList, viewModel.bookList.getValue());
        mockedLog.verify(() -> Log.e(anyString(), anyString()), never());
    }

    /**
     * loadBooks: 書籍リストの取得中にExecutionExceptionが発生した場合
     * _bookList LiveDataがnullで更新され、エラーログが出力されることをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void loadBooks_executionException_postsNullAndLogsError() throws Exception {
        // Given
        when(mockBookRepository.getAllBooks(anyString())).thenReturn(mockBooksFuture);
        when(mockBooksFuture.get()).thenThrow(new ExecutionException(new Throwable("Test Exception")));

        // When
        viewModel.loadBooks("uid");

        // Then
        verify(mockBookRepository).getAllBooks(eq("uid"));
        Thread.sleep(100); // 非同期処理完了を待機
        assertEquals(null, viewModel.bookList.getValue());
        mockedLog.verify(() -> Log.e(eq("BookListViewModel"), contains("Error loading books")), times(1));
    }

    /**
     * loadBooks: 書籍リストの取得中にInterruptedExceptionが発生した場合
     * _bookList LiveDataがnullで更新され、エラーログが出力されることをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void loadBooks_interruptedException_postsNullAndLogsError() throws Exception {
        // Given
        when(mockBookRepository.getAllBooks(anyString())).thenReturn(mockBooksFuture);
        when(mockBooksFuture.get()).thenThrow(new InterruptedException("Test Interrupted"));

        // When
        viewModel.loadBooks("uid");

        // Then
        verify(mockBookRepository).getAllBooks(eq("uid"));
        Thread.sleep(100); // 非同期処理完了を待機
        assertEquals(null, viewModel.bookList.getValue());
        mockedLog.verify(() -> Log.e(eq("BookListViewModel"), contains("Error loading books")), times(1));
    }

    /**
     * isEmpty: LiveDataが空のリストを保持している場合
     * trueを返すことをテスト
     */
    @Test
    public void isEmpty_emptyList_returnsTrue() throws Exception {
        // Given
        List<BookSummaryData> emptyList = new ArrayList<>();
        when(mockBookRepository.getAllBooks(anyString())).thenReturn(mockBooksFuture);
        when(mockBooksFuture.get()).thenReturn(emptyList);
        viewModel.loadBooks("uid");
        Thread.sleep(100); // 非同期処理完了を待機

        // When
        boolean result = viewModel.isEmpty();

        // Then
        assertTrue(result);
    }

    /**
     * isEmpty: LiveDataがnullを保持している場合
     * trueを返すことをテスト
     */
    @Test
    public void isEmpty_nullList_returnsTrue() throws Exception {
        // Given
        when(mockBookRepository.getAllBooks(anyString())).thenReturn(mockBooksFuture);
        when(mockBooksFuture.get()).thenReturn(null);
        viewModel.loadBooks("uid");
        Thread.sleep(100); // 非同期処理完了を待機

        // When
        boolean result = viewModel.isEmpty();

        // Then
        assertTrue(result);
    }

    /**
     * isEmpty: LiveDataが空でないリストを保持している場合
     * falseを返すことをテスト
     */
    @Test
    public void isEmpty_nonEmptyList_returnsFalse() throws Exception {
        // Given
        List<BookSummaryData> nonEmptyList = Arrays.asList(new BookSummaryData("id1", "Title 1", "url1"));
        when(mockBookRepository.getAllBooks(anyString())).thenReturn(mockBooksFuture);
        when(mockBooksFuture.get()).thenReturn(nonEmptyList);
        viewModel.loadBooks("uid");
        Thread.sleep(100); // 非同期処理完了を待機

        // When
        boolean result = viewModel.isEmpty();

        // Then
        assertFalse(result);
    }

    /**
     * updatePublicStatus: 公開ステータスの更新が成功した場合
     * 成功ログが出力され、書籍リストが再ロードされることをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void updatePublicStatus_success_logsSuccessAndReloadsBooks() throws Exception {
        // Given
        String uid = "testUid";
        String volumeId = "testVolumeId";
        boolean newPublicStatus = true;

        when(mockBookRepository.updateBookPublicStatus(anyString(), anyString(), anyBoolean()))
                .thenReturn(mockUpdatePublicStatusFuture);
        when(mockUpdatePublicStatusFuture.get()).thenReturn(true); // 更新成功

        // loadBooksが呼ばれた時に返される書籍リストのモック
        List<BookSummaryData> reloadedBooks = Arrays.asList(new BookSummaryData("id1", "Title 1", "url1"));
        Future<List<BookSummaryData>> reloadedFuture = mock(Future.class);
        when(mockBookRepository.getAllBooks(anyString())).thenReturn(reloadedFuture);
        when(reloadedFuture.get()).thenReturn(reloadedBooks);

        // When
        viewModel.updatePublicStatus(uid, volumeId, newPublicStatus);

        // Then
        verify(mockBookRepository).updateBookPublicStatus(eq(uid), eq(volumeId), eq(newPublicStatus));
        Thread.sleep(100); // 非同期処理完了を待機

        mockedLog.verify(() -> Log.d(eq("BookListViewModel"), contains("Public status updated successfully")), times(1));
        // 更新成功後にloadBooksが呼ばれるため、getAllBooksが再度呼ばれる
        verify(mockBookRepository, times(1)).getAllBooks(eq(uid)); // 1回目はテスト設定のため、2回目はreloadのため
        // loadBooksの結果がLiveDataに反映されることを検証
        Thread.sleep(100); // reload処理完了を待機
        assertEquals(reloadedBooks, viewModel.bookList.getValue());
        mockedLog.verify(() -> Log.e(anyString(), anyString()), never());
    }

    /**
     * updatePublicStatus: 公開ステータスの更新が失敗した場合 (repositoryがfalseを返す)
     * 失敗ログが出力され、書籍リストが再ロードされないことをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void updatePublicStatus_failure_logsFailureAndDoesNotReloadBooks() throws Exception {
        // Given
        String uid = "testUid";
        String volumeId = "testVolumeId";
        boolean newPublicStatus = false;

        when(mockBookRepository.updateBookPublicStatus(anyString(), anyString(), anyBoolean()))
                .thenReturn(mockUpdatePublicStatusFuture);
        when(mockUpdatePublicStatusFuture.get()).thenReturn(false); // 更新失敗

        // When
        viewModel.updatePublicStatus(uid, volumeId, newPublicStatus);

        // Then
        verify(mockBookRepository).updateBookPublicStatus(eq(uid), eq(volumeId), eq(newPublicStatus));
        Thread.sleep(100); // 非同期処理完了を待機

        mockedLog.verify(() -> Log.e(eq("BookListViewModel"), contains("Failed to update public status")), times(1));
        // 更新失敗の場合、loadBooksは呼ばれないことを検証
        verify(mockBookRepository, never()).getAllBooks(anyString());
    }

    /**
     * updatePublicStatus: 公開ステータスの更新中にExecutionExceptionが発生した場合
     * エラーログが出力され、書籍リストが再ロードされないことをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void updatePublicStatus_executionException_logsErrorAndDoesNotReloadBooks() throws Exception {
        // Given
        String uid = "testUid";
        String volumeId = "testVolumeId";
        boolean newPublicStatus = true;

        when(mockBookRepository.updateBookPublicStatus(anyString(), anyString(), anyBoolean()))
                .thenReturn(mockUpdatePublicStatusFuture);
        when(mockUpdatePublicStatusFuture.get()).thenThrow(new ExecutionException(new Throwable("Test Exception")));

        // When
        viewModel.updatePublicStatus(uid, volumeId, newPublicStatus);

        // Then
        verify(mockBookRepository).updateBookPublicStatus(eq(uid), eq(volumeId), eq(newPublicStatus));
        Thread.sleep(100); // 非同期処理完了を待機

        mockedLog.verify(() -> Log.e(eq("BookListViewModel"), contains("Error updating public status")), times(1));
        // 更新失敗の場合、loadBooksは呼ばれないことを検証
        verify(mockBookRepository, never()).getAllBooks(anyString());
    }
}