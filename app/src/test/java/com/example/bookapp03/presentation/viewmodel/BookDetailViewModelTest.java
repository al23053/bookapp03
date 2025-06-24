package com.example.bookapp03.presentation.viewmodel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import android.util.Log;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule; // LiveDataテスト用
import androidx.lifecycle.Observer;

import com.example.bookapp03.data.model.BookDetailData;
import com.example.bookapp03.data.model.HighlightMemoData;
import com.example.bookapp03.domain.repository.BookRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.Rule; // LiveDataテスト用

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture; // Futureのモック用

@ExtendWith(MockitoExtension.class)
public class BookDetailViewModelTest {

    @Rule // LiveDataのバックグラウンドタスクを同期的に実行
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private BookRepository mockBookRepository;
    @Mock
    private Observer<BookDetailData> mockBookDetailObserver;
    @Mock
    private Observer<List<HighlightMemoData>> mockHighlightMemosObserver;

    private BookDetailViewModel viewModel;

    @BeforeEach
    void setUp() {
        // Log.e をモック化
        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            mockedLog.when(() -> Log.e(anyString(), anyString())).thenReturn(0);
            mockedLog.when(() -> Log.e(anyString(), anyString(), any(Throwable.class))).thenReturn(0);
            mockedLog.when(() -> Log.d(anyString(), anyString())).thenReturn(0);
        }

        viewModel = new BookDetailViewModel(mockBookRepository);
        viewModel.bookDetail.observeForever(mockBookDetailObserver);
        viewModel.highlightMemos.observeForever(mockHighlightMemosObserver);
    }

    @Test
    void testLoadBookDetail_success() {
        String uid = "user1";
        String volumeId = "vol1";
        BookDetailData expectedData = new BookDetailData(volumeId, "Test Book", "Test Summary", "url", "public");

        // mockBookRepository.getBookDetail が CompletableFuture で結果を返すように設定
        when(mockBookRepository.getBookDetail(uid, volumeId))
                .thenReturn(CompletableFuture.completedFuture(expectedData));

        viewModel.loadBookDetail(uid, volumeId);

        // LiveDataのpostValueが非同期で行われるため、少し待つか、InstantTaskExecutorRule を使う
        // InstantTaskExecutorRule があれば即座に実行される
        verify(mockBookRepository).getBookDetail(uid, volumeId);
        verify(mockBookDetailObserver).onChanged(expectedData);
        assertEquals(expectedData, viewModel.bookDetail.getValue());
    }

    @Test
    void testLoadBookDetail_notFound() {
        String uid = "user1";
        String volumeId = "vol1";

        when(mockBookRepository.getBookDetail(uid, volumeId))
                .thenReturn(CompletableFuture.completedFuture(null));

        viewModel.loadBookDetail(uid, volumeId);

        verify(mockBookRepository).getBookDetail(uid, volumeId);
        verify(mockBookDetailObserver).onChanged(null);
        assertNull(viewModel.bookDetail.getValue());
    }

    @Test
    void testLoadBookDetail_exception() {
        String uid = "user1";
        String volumeId = "vol1";

        when(mockBookRepository.getBookDetail(uid, volumeId))
                .thenReturn(CompletableFuture.supplyAsync(() -> { throw new RuntimeException("Test Exception"); }));

        viewModel.loadBookDetail(uid, volumeId);

        verify(mockBookRepository).getBookDetail(uid, volumeId);
        // エラー時はnullがpostされることを確認
        verify(mockBookDetailObserver).onChanged(null);
        assertNull(viewModel.bookDetail.getValue());
    }

    @Test
    void testUpdateBookPublicStatus_success() {
        String uid = "user1";
        String volumeId = "vol1";
        boolean isPublic = true;
        BookDetailData originalData = new BookDetailData(volumeId, "Test Book", "Test Summary", "url", "private");
        BookDetailData updatedData = new BookDetailData(volumeId, "Test Book", "Test Summary", "url", "public");

        when(mockBookRepository.updateBookPublicStatus(uid, volumeId, isPublic))
                .thenReturn(CompletableFuture.completedFuture(true));
        when(mockBookRepository.getBookDetail(uid, volumeId))
                .thenReturn(CompletableFuture.completedFuture(updatedData));

        // ViewModelに初期データをセット (更新前)
        viewModel.bookDetail.setValue(originalData);

        viewModel.updateBookPublicStatus(uid, volumeId, isPublic);

        verify(mockBookRepository).updateBookPublicStatus(uid, volumeId, isPublic);
        // updateBookPublicStatus成功後にloadBookDetailが呼ばれることを検証
        verify(mockBookRepository).getBookDetail(uid, volumeId);
        verify(mockBookDetailObserver, atLeastOnce()).onChanged(updatedData);
        assertEquals(updatedData, viewModel.bookDetail.getValue());
    }

    @Test
    void testUpdateBookPublicStatus_failure() {
        String uid = "user1";
        String volumeId = "vol1";
        boolean isPublic = false;
        BookDetailData originalData = new BookDetailData(volumeId, "Test Book", "Test Summary", "url", "public");

        when(mockBookRepository.updateBookPublicStatus(uid, volumeId, isPublic))
                .thenReturn(CompletableFuture.completedFuture(false));

        viewModel.bookDetail.setValue(originalData); // 初期データセット

        viewModel.updateBookPublicStatus(uid, volumeId, isPublic);

        verify(mockBookRepository).updateBookPublicStatus(uid, volumeId, isPublic);
        // 失敗時はloadBookDetailが呼ばれないこと (または元のデータが維持されること)
        verify(mockBookRepository, never()).getBookDetail(uid, volumeId);
        assertEquals(originalData, viewModel.bookDetail.getValue()); // データが変更されていないことを確認
    }

    @Test
    void testLoadHighlightMemos_success() {
        String uid = "user1";
        String volumeId = "vol1";
        List<HighlightMemoData> expectedMemos = Arrays.asList(
                new HighlightMemoData(1, 10, "Memo A"),
                new HighlightMemoData(2, 20, "Memo B")
        );

        when(mockBookRepository.getHighlightMemos(uid, volumeId))
                .thenReturn(CompletableFuture.completedFuture(expectedMemos));

        viewModel.loadHighlightMemos(uid, volumeId);

        verify(mockBookRepository).getHighlightMemos(uid, volumeId);
        verify(mockHighlightMemosObserver).onChanged(expectedMemos);
        assertEquals(expectedMemos, viewModel.highlightMemos.getValue());
    }

    @Test
    void testLoadHighlightMemos_empty() {
        String uid = "user1";
        String volumeId = "vol1";
        List<HighlightMemoData> expectedMemos = new ArrayList<>();

        when(mockBookRepository.getHighlightMemos(uid, volumeId))
                .thenReturn(CompletableFuture.completedFuture(expectedMemos));

        viewModel.loadHighlightMemos(uid, volumeId);

        verify(mockBookRepository).getHighlightMemos(uid, volumeId);
        verify(mockHighlightMemosObserver).onChanged(expectedMemos);
        assertEquals(expectedMemos, viewModel.highlightMemos.getValue());
        assertTrue(viewModel.highlightMemos.getValue().isEmpty());
    }

    @Test
    void testLoadHighlightMemos_exception() {
        String uid = "user1";
        String volumeId = "vol1";

        when(mockBookRepository.getHighlightMemos(uid, volumeId))
                .thenReturn(CompletableFuture.supplyAsync(() -> { throw new RuntimeException("Memo Load Error"); }));

        viewModel.loadHighlightMemos(uid, volumeId);

        verify(mockBookRepository).getHighlightMemos(uid, volumeId);
        verify(mockHighlightMemosObserver).onChanged(null); // エラー時はnullをpost
        assertNull(viewModel.highlightMemos.getValue());
    }

    @Test
    void testRegisterHighlightMemo_success() {
        String uid = "user1";
        String volumeId = "vol1";
        HighlightMemoData memoToRegister = new HighlightMemoData(3, 30, "New Memo");
        List<HighlightMemoData> memosAfterRegistration = Arrays.asList(
                new HighlightMemoData(1, 10, "Memo A"),
                new HighlightMemoData(3, 30, "New Memo") // 登録後のリスト
        );

        when(mockBookRepository.registerHighlightMemo(uid, volumeId, memoToRegister))
                .thenReturn(CompletableFuture.completedFuture(true));
        when(mockBookRepository.getHighlightMemos(uid, volumeId))
                .thenReturn(CompletableFuture.completedFuture(memosAfterRegistration));

        viewModel.registerHighlightMemo(uid, volumeId, memoToRegister);

        verify(mockBookRepository).registerHighlightMemo(uid, volumeId, memoToRegister);
        // 登録成功後、メモリストが再ロードされることを検証
        verify(mockBookRepository).getHighlightMemos(uid, volumeId);
        verify(mockHighlightMemosObserver).onChanged(memosAfterRegistration);
        assertEquals(memosAfterRegistration, viewModel.highlightMemos.getValue());
    }

    @Test
    void testRegisterHighlightMemo_failure() {
        String uid = "user1";
        String volumeId = "vol1";
        HighlightMemoData memoToRegister = new HighlightMemoData(3, 30, "New Memo");
        List<HighlightMemoData> currentMemos = Arrays.asList(new HighlightMemoData(1, 10, "Memo A"));

        when(mockBookRepository.registerHighlightMemo(uid, volumeId, memoToRegister))
                .thenReturn(CompletableFuture.completedFuture(false));

        viewModel.highlightMemos.setValue(currentMemos); // 登録前の状態をセット

        viewModel.registerHighlightMemo(uid, volumeId, memoToRegister);

        verify(mockBookRepository).registerHighlightMemo(uid, volumeId, memoToRegister);
        // 登録失敗時はメモリストが再ロードされないことを検証
        verify(mockBookRepository, never()).getHighlightMemos(uid, volumeId);
        assertEquals(currentMemos, viewModel.highlightMemos.getValue()); // データが変更されていないことを確認
    }

    @Test
    void testRegisterHighlightMemo_exception() {
        String uid = "user1";
        String volumeId = "vol1";
        HighlightMemoData memoToRegister = new HighlightMemoData(3, 30, "New Memo");
        List<HighlightMemoData> currentMemos = Arrays.asList(new HighlightMemoData(1, 10, "Memo A"));

        when(mockBookRepository.registerHighlightMemo(uid, volumeId, memoToRegister))
                .thenReturn(CompletableFuture.supplyAsync(() -> { throw new RuntimeException("Register Error"); }));

        viewModel.highlightMemos.setValue(currentMemos);

        viewModel.registerHighlightMemo(uid, volumeId, memoToRegister);

        verify(mockBookRepository).registerHighlightMemo(uid, volumeId, memoToRegister);
        // エラー発生時はメモリストが再ロードされないことを検証
        verify(mockBookRepository, never()).getHighlightMemos(uid, volumeId);
        assertEquals(currentMemos, viewModel.highlightMemos.getValue()); // データが変更されていないことを確認
    }
}