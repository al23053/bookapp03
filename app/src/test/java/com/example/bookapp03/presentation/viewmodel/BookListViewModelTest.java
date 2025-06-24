package com.example.bookapp03.presentation.viewmodel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import android.util.Log;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.bookapp03.data.model.BookSummaryData;
import com.example.bookapp03.domain.repository.BookRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.Rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ExtendWith(MockitoExtension.class)
public class BookListViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private BookRepository mockBookRepository;
    @Mock
    private Observer<List<BookSummaryData>> mockBookListObserver;

    private BookListViewModel viewModel;

    @BeforeEach
    void setUp() {
        // Log.e, Log.d をモック化
        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            mockedLog.when(() -> Log.e(anyString(), anyString())).thenReturn(0);
            mockedLog.when(() -> Log.e(anyString(), anyString(), any(Throwable.class))).thenReturn(0);
            mockedLog.when(() -> Log.d(anyString(), anyString())).thenReturn(0);
        }

        viewModel = new BookListViewModel(mockBookRepository);
        viewModel.bookList.observeForever(mockBookListObserver);
    }

    @Test
    void testLoadBooks_success() {
        String uid = "user1";
        List<BookSummaryData> expectedList = Arrays.asList(
                new BookSummaryData("vol1", "Title A", "urlA"),
                new BookSummaryData("vol2", "Title B", "urlB")
        );
        expectedList.get(0).setPublic(true); // 仮の状態設定

        when(mockBookRepository.getAllBookSummaries(uid))
                .thenReturn(CompletableFuture.completedFuture(expectedList));

        viewModel.loadBooks(uid);

        verify(mockBookRepository).getAllBookSummaries(uid);
        verify(mockBookListObserver).onChanged(expectedList);
        assertEquals(expectedList, viewModel.bookList.getValue());
    }

    @Test
    void testLoadBooks_empty() {
        String uid = "user1";
        List<BookSummaryData> expectedList = Collections.emptyList();

        when(mockBookRepository.getAllBookSummaries(uid))
                .thenReturn(CompletableFuture.completedFuture(expectedList));

        viewModel.loadBooks(uid);

        verify(mockBookRepository).getAllBookSummaries(uid);
        verify(mockBookListObserver).onChanged(expectedList);
        assertEquals(expectedList, viewModel.bookList.getValue());
        assertTrue(viewModel.isEmpty());
    }

    @Test
    void testLoadBooks_exception() {
        String uid = "user1";

        when(mockBookRepository.getAllBookSummaries(uid))
                .thenReturn(CompletableFuture.supplyAsync(() -> { throw new RuntimeException("Book Load Error"); }));

        viewModel.loadBooks(uid);

        verify(mockBookRepository).getAllBookSummaries(uid);
        verify(mockBookListObserver).onChanged(null); // エラー時はnullがpostされる
        assertNull(viewModel.bookList.getValue());
    }

    @Test
    void testIsEmpty_withBooks() {
        List<BookSummaryData> currentList = Arrays.asList(new BookSummaryData("vol1", "Title A", "urlA"));
        viewModel.bookList.setValue(currentList);
        assertFalse(viewModel.isEmpty());
    }

    @Test
    void testIsEmpty_noBooks() {
        viewModel.bookList.setValue(Collections.emptyList());
        assertTrue(viewModel.isEmpty());

        viewModel.bookList.setValue(null);
        assertTrue(viewModel.isEmpty());
    }

    @Test
    void testUpdatePublicStatus_success() {
        String uid = "user1";
        String volumeId = "vol1";
        boolean newPublicStatus = true;
        List<BookSummaryData> initialList = Arrays.asList(
                new BookSummaryData("vol1", "Title A", "urlA"),
                new BookSummaryData("vol2", "Title B", "urlB")
        );
        initialList.get(0).setPublic(false); // 初期状態

        List<BookSummaryData> updatedList = Arrays.asList(
                new BookSummaryData("vol1", "Title A", "urlA"),
                new BookSummaryData("vol2", "Title B", "urlB")
        );
        updatedList.get(0).setPublic(true); // 更新後の状態

        when(mockBookRepository.updateBookPublicStatus(uid, volumeId, newPublicStatus))
                .thenReturn(CompletableFuture.completedFuture(true));
        when(mockBookRepository.getAllBookSummaries(uid))
                .thenReturn(CompletableFuture.completedFuture(updatedList));

        viewModel.bookList.setValue(initialList); // ViewModelに初期リストをセット

        viewModel.updatePublicStatus(uid, volumeId, newPublicStatus);

        verify(mockBookRepository).updateBookPublicStatus(uid, volumeId, newPublicStatus);
        // 更新成功後、loadBooksが呼ばれ、リストが再ロードされることを検証
        verify(mockBookRepository).getAllBookSummaries(uid);
        verify(mockBookListObserver, atLeastOnce()).onChanged(updatedList);
        assertEquals(updatedList, viewModel.bookList.getValue());
    }

    @Test
    void testUpdatePublicStatus_failure() {
        String uid = "user1";
        String volumeId = "vol1";
        boolean newPublicStatus = false;
        List<BookSummaryData> initialList = Arrays.asList(
                new BookSummaryData("vol1", "Title A", "urlA")
        );
        initialList.get(0).setPublic(true);

        when(mockBookRepository.updateBookPublicStatus(uid, volumeId, newPublicStatus))
                .thenReturn(CompletableFuture.completedFuture(false));

        viewModel.bookList.setValue(initialList);

        viewModel.updatePublicStatus(uid, volumeId, newPublicStatus);

        verify(mockBookRepository).updateBookPublicStatus(uid, volumeId, newPublicStatus);
        // 失敗時はloadBooksが呼ばれないこと (または元のデータが維持されること)
        verify(mockBookRepository, never()).getAllBookSummaries(uid);
        assertEquals(initialList, viewModel.bookList.getValue()); // データが変更されていないことを確認
    }

    @Test
    void testUpdatePublicStatus_exception() {
        String uid = "user1";
        String volumeId = "vol1";
        boolean newPublicStatus = true;
        List<BookSummaryData> initialList = Arrays.asList(
                new BookSummaryData("vol1", "Title A", "urlA")
        );
        initialList.get(0).setPublic(false);

        when(mockBookRepository.updateBookPublicStatus(uid, volumeId, newPublicStatus))
                .thenReturn(CompletableFuture.supplyAsync(() -> { throw new RuntimeException("Update Error"); }));

        viewModel.bookList.setValue(initialList);

        viewModel.updatePublicStatus(uid, volumeId, newPublicStatus);

        verify(mockBookRepository).updateBookPublicStatus(uid, volumeId, newPublicStatus);
        // エラー発生時はloadBooksが呼ばれないこと
        verify(mockBookRepository, never()).getAllBookSummaries(uid);
        assertEquals(initialList, viewModel.bookList.getValue()); // データが変更されていないことを確認
    }
}