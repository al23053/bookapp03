package com.example.bookapp03.presentation.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import com.example.bookapp03.data.model.BookDetailData;
import com.example.bookapp03.C1UIProcessing.HighlightMemoData;
import com.example.bookapp03.domain.repository.BookRepository;

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

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class BookDetailViewModelTest {

    private BookDetailViewModel viewModel;

    @Mock
    private BookRepository mockBookRepository;
    @Mock
    private Future<BookDetailData> mockBookDetailFuture;
    @Mock
    private Future<List<HighlightMemoData>> mockHighlightMemosFuture;
    @Mock
    private Future<Boolean> mockRegisterMemoFuture;

    // LiveDataのpostValueを検証するためにSpy化
    private MutableLiveData<BookDetailData> spyBookDetailLiveData;
    private MutableLiveData<List<HighlightMemoData>> spyHighlightMemosLiveData;

    // Log.e と Log.d をモックするためのMockedStatic
    private MockedStatic<Log> mockedLog;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        // BookDetailViewModelのコンストラクタはMutableLiveDataを内部で初期化するため、
        // Spyを使ってそのpostValueを検証できるようにする
        // リフレクションを使って_bookDetailと_highlightMemosにアクセスしてspyに置き換える
        viewModel = new BookDetailViewModel(mockBookRepository);

        // MutableLiveDataのインスタンスをSpy化して置き換え
        // ここでは_bookDetailと_highlightMemosがprivateなので、直接アクセスできないが、
        // テスト環境ではpublicとして扱われるか、あるいは以下のように間接的に検証
        // このテストでは、ViewModelのpublicなLiveDataフィールドにアクセスし、
        // そのgetValue()をチェックするのではなく、内部の_bookDetail / _highlightMemos
        // のpostValue()が呼ばれたことをverifyする方針。
        // ViewModelの_bookDetailと_highlightMemosはfinalでコンストラクタで初期化されるため、
        // spy化するためには直接アクセスして置き換えるか、あるいはverify(liveData).postValue()を使う。
        // ここでは、ViewModelのインスタンスが生成された後に、内部のMutableLiveDataがspy化されるように修正。
        // もしくは、ViewModelのコンストラクタでLiveDatを外部から注入可能にするか。
        // 今回のコードではprivate final なので、そのままverifyする。
        // Robolectric環境では、Threadの実行が同期的に行われるため、Future.get()の結果がすぐに反映される。

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
     * loadBookDetail: 書籍詳細データの取得が成功した場合
     * _bookDetail LiveDataが正しいデータで更新されることをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void loadBookDetail_success_postsBookDetailData() throws Exception {
        // Given
        BookDetailData testData = new BookDetailData("id1", "Test Book", "Summary", "url", "public");
        when(mockBookRepository.getBookDetail(anyString(), anyString())).thenReturn(mockBookDetailFuture);
        when(mockBookDetailFuture.get()).thenReturn(testData);

        // When
        viewModel.loadBookDetail("uid", "volumeId1");

        // Then
        // repositoryからデータが取得されたことを検証
        verify(mockBookRepository).getBookDetail(eq("uid"), eq("volumeId1"));
        // _bookDetail LiveDataが正しいデータでpostValueされることを検証
        // ViewModel内の_bookDetailはprivate finalなので、ここではpublicなbookDetailにアクセスしてgetValue()を検証する。
        // Robolectric環境ではpostValueはメインスレッドで実行される。
        Thread.sleep(100); // 非同期処理完了を待機
        assertEquals(testData, viewModel.bookDetail.getValue());
        mockedLog.verify(() -> Log.e(anyString(), anyString()), never()); // エラーログは出力されない
    }

    /**
     * loadBookDetail: 書籍詳細データの取得中にExecutionExceptionが発生した場合
     * _bookDetail LiveDataがnullで更新され、エラーログが出力されることをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void loadBookDetail_executionException_postsNullAndLogsError() throws Exception {
        // Given
        when(mockBookRepository.getBookDetail(anyString(), anyString())).thenReturn(mockBookDetailFuture);
        when(mockBookDetailFuture.get()).thenThrow(new ExecutionException(new Throwable("Test Exception")));

        // When
        viewModel.loadBookDetail("uid", "volumeId1");

        // Then
        verify(mockBookRepository).getBookDetail(eq("uid"), eq("volumeId1"));
        Thread.sleep(100); // 非同期処理完了を待機
        assertEquals(null, viewModel.bookDetail.getValue());
        mockedLog.verify(() -> Log.e(eq("BookDetailViewModel"), contains("Error loading book detail")), times(1));
    }

    /**
     * loadBookDetail: 書籍詳細データの取得中にInterruptedExceptionが発生した場合
     * _bookDetail LiveDataがnullで更新され、エラーログが出力されることをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void loadBookDetail_interruptedException_postsNullAndLogsError() throws Exception {
        // Given
        when(mockBookRepository.getBookDetail(anyString(), anyString())).thenReturn(mockBookDetailFuture);
        when(mockBookDetailFuture.get()).thenThrow(new InterruptedException("Test Interrupted"));

        // When
        viewModel.loadBookDetail("uid", "volumeId1");

        // Then
        verify(mockBookRepository).getBookDetail(eq("uid"), eq("volumeId1"));
        Thread.sleep(100); // 非同期処理完了を待機
        assertEquals(null, viewModel.bookDetail.getValue());
        mockedLog.verify(() -> Log.e(eq("BookDetailViewModel"), contains("Error loading book detail")), times(1));
    }

    /**
     * loadHighlightMemos: ハイライトメモの取得が成功した場合
     * _highlightMemos LiveDataが正しいデータで更新されることをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void loadHighlightMemos_success_postsHighlightMemos() throws Exception {
        // Given
        List<HighlightMemoData> testMemos = Arrays.asList(
                new HighlightMemoData(1, 10, "Memo 1"),
                new HighlightMemoData(2, 20, "Memo 2")
        );
        when(mockBookRepository.getHighlightMemos(anyString(), anyString())).thenReturn(mockHighlightMemosFuture);
        when(mockHighlightMemosFuture.get()).thenReturn(testMemos);

        // When
        viewModel.loadHighlightMemos("uid", "volumeId1");

        // Then
        verify(mockBookRepository).getHighlightMemos(eq("uid"), eq("volumeId1"));
        Thread.sleep(100); // 非同期処理完了を待機
        assertEquals(testMemos, viewModel.highlightMemos.getValue());
        mockedLog.verify(() -> Log.e(anyString(), anyString()), never());
    }

    /**
     * loadHighlightMemos: ハイライトメモの取得中にExecutionExceptionが発生した場合
     * _highlightMemos LiveDataがnullで更新され、エラーログが出力されることをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void loadHighlightMemos_executionException_postsNullAndLogsError() throws Exception {
        // Given
        when(mockBookRepository.getHighlightMemos(anyString(), anyString())).thenReturn(mockHighlightMemosFuture);
        when(mockHighlightMemosFuture.get()).thenThrow(new ExecutionException(new Throwable("Test Exception")));

        // When
        viewModel.loadHighlightMemos("uid", "volumeId1");

        // Then
        verify(mockBookRepository).getHighlightMemos(eq("uid"), eq("volumeId1"));
        Thread.sleep(100); // 非同期処理完了を待機
        assertEquals(null, viewModel.highlightMemos.getValue());
        mockedLog.verify(() -> Log.e(eq("BookDetailViewModel"), contains("Error loading highlight memos")), times(1));
    }

    /**
     * registerHighlightMemo: メモの登録が成功した場合
     * 成功ログが出力され、メモリストが再ロードされることをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void registerHighlightMemo_success_logsSuccessAndReloadsMemos() throws Exception {
        // Given
        HighlightMemoData testMemo = new HighlightMemoData(1, 1, "New Memo");
        when(mockBookRepository.registerHighlightMemo(anyString(), anyString(), any(HighlightMemoData.class))).thenReturn(mockRegisterMemoFuture);
        when(mockRegisterMemoFuture.get()).thenReturn(true); // 登録成功

        // loadHighlightMemosが呼ばれた時に返されるメモリストのモック
        List<HighlightMemoData> reloadedMemos = Arrays.asList(testMemo);
        Future<List<HighlightMemoData>> reloadedFuture = mock(Future.class);
        when(mockBookRepository.getHighlightMemos(anyString(), anyString())).thenReturn(reloadedFuture);
        when(reloadedFuture.get()).thenReturn(reloadedMemos);

        // When
        viewModel.registerHighlightMemo("uid", "volumeId1", testMemo);

        // Then
        verify(mockBookRepository).registerHighlightMemo(eq("uid"), eq("volumeId1"), eq(testMemo));
        Thread.sleep(100); // 非同期処理完了を待機

        mockedLog.verify(() -> Log.d(eq("BookDetailViewModel"), eq("Highlight memo registered successfully.")), times(1));
        // 登録成功後にloadHighlightMemosが呼ばれるため、getHighlightMemosが再度呼ばれる
        verify(mockBookRepository, times(1)).getHighlightMemos(eq("uid"), eq("volumeId1")); // 1回目はテスト設定のため、2回目はreloadのため
        // loadHighlightMemosの結果がLiveDataに反映されることを検証
        Thread.sleep(100); // reload処理完了を待機
        assertEquals(reloadedMemos, viewModel.highlightMemos.getValue());
        mockedLog.verify(() -> Log.e(anyString(), anyString()), never());
    }

    /**
     * registerHighlightMemo: メモの登録が失敗した場合 (repositoryがfalseを返す)
     * 失敗ログが出力され、メモリストが再ロードされないことをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void registerHighlightMemo_failure_logsFailureAndDoesNotReloadMemos() throws Exception {
        // Given
        HighlightMemoData testMemo = new HighlightMemoData(1, 1, "New Memo");
        when(mockBookRepository.registerHighlightMemo(anyString(), anyString(), any(HighlightMemoData.class))).thenReturn(mockRegisterMemoFuture);
        when(mockRegisterMemoFuture.get()).thenReturn(false); // 登録失敗

        // When
        viewModel.registerHighlightMemo("uid", "volumeId1", testMemo);

        // Then
        verify(mockBookRepository).registerHighlightMemo(eq("uid"), eq("volumeId1"), eq(testMemo));
        Thread.sleep(100); // 非同期処理完了を待機

        mockedLog.verify(() -> Log.e(eq("BookDetailViewModel"), eq("Failed to register highlight memo.")), times(1));
        // 登録失敗の場合、loadHighlightMemosは呼ばれないことを検証
        verify(mockBookRepository, never()).getHighlightMemos(anyString(), anyString());
    }

    /**
     * registerHighlightMemo: メモの登録中にExecutionExceptionが発生した場合
     * エラーログが出力され、メモリストが再ロードされないことをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void registerHighlightMemo_executionException_logsErrorAndDoesNotReloadMemos() throws Exception {
        // Given
        HighlightMemoData testMemo = new HighlightMemoData(1, 1, "New Memo");
        when(mockBookRepository.registerHighlightMemo(anyString(), anyString(), any(HighlightMemoData.class))).thenReturn(mockRegisterMemoFuture);
        when(mockRegisterMemoFuture.get()).thenThrow(new ExecutionException(new Throwable("Test Exception")));

        // When
        viewModel.registerHighlightMemo("uid", "volumeId1", testMemo);

        // Then
        verify(mockBookRepository).registerHighlightMemo(eq("uid"), eq("volumeId1"), eq(testMemo));
        Thread.sleep(100); // 非同期処理完了を待機

        mockedLog.verify(() -> Log.e(eq("BookDetailViewModel"), contains("Error registering highlight memo")), times(1));
        // 登録失敗の場合、loadHighlightMemosは呼ばれないことを検証
        verify(mockBookRepository, never()).getHighlightMemos(anyString(), anyString());
    }
}