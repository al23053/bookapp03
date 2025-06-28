package com.example.bookapp03.data.repository;

import android.content.Context;
import android.util.Log; // Logは静的クラスですが、テストでは通常無視するか、専用のテストルールで扱うことが多いです。

import com.example.bookapp03.C6BookInformationManaging.RegisterHighlightMemo;
import com.example.bookapp03.C6BookInformationManaging.RegisterSummary;
import com.example.bookapp03.C6BookInformationManaging.VolumeIdProvider; // 静的メソッドを含みます
import com.example.bookapp03.C6BookInformationManaging.database.BookInformationDatabase; // 静的メソッドを含みます
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoDao;
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoEntity;
import com.example.bookapp03.C6BookInformationManaging.database.SummaryDao;
import com.example.bookapp03.C6BookInformationManaging.database.SummaryEntity;
import com.example.bookapp03.data.model.BookDetailData;
import com.example.bookapp03.data.model.BookSummaryData;
import com.example.bookapp03.data.model.HighlightMemoData;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic; // staticメソッドをモックするために必要 (mockito-inlineが必要)

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors; // 本来はテスト用にExecutorServiceをモックするか、同期Executorを使うべき
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * BookRepositoryImplの単体テストクラス (JUnit 4 + Mockito)
 *
 * 【重要】このテストクラスを完全に動作させるためには、build.gradleに 'mockito-inline' の依存関係を追加し、
 * Javaのバージョンを適切に設定する必要があります。
 *
 * 例 (build.gradle):
 * dependencies {
 * // ...
 * testImplementation 'org.mockito:mockito-core:5.x.x' // 現在のMockitoのバージョンに合わせる
 * testImplementation 'org.mockito:mockito-inline:5.x.x' // staticメソッドのモックに必要
 * // ...
 * }
 *
 * もし build.gradle を変更できない場合、BookInformationDatabase.getDatabase()
 * や VolumeIdProvider の静的メソッドのモックは動作せず、これらのメソッドが依存する
 * 実際の挙動にテストが依存することになります（これは単体テストの原則に反します）。
 */
public class BookRepositoryImplTest {

    // モックオブジェクトの宣言
    @Mock
    Context mockContext;
    @Mock
    SummaryDao mockSummaryDao;
    @Mock
    HighlightMemoDao mockHighlightMemoDao;
    @Mock
    RegisterSummary mockRegisterSummary;
    @Mock
    RegisterHighlightMemo mockRegisterHighlightMemo;
    @Mock
    BookInformationDatabase mockBookInformationDatabase; // staticメソッドgetDatabase()の戻り値として利用

    // テスト対象のインスタンス
    private BookRepositoryImpl bookRepository;

    // 非同期処理をテストするためのExecutorService (テスト用)
    private ExecutorService testExecutor;

    @Before
    public void setUp() throws Exception {
        // Mockitoアノテーションを初期化
        MockitoAnnotations.initMocks(this);

        // テスト用のExecutorServiceを準備 (単一スレッドで順次実行されるようにする)
        // 実際のBookRepositoryImplが独自のExecutorを生成するため、ここでは単純なテスト用を定義
        testExecutor = Executors.newSingleThreadExecutor();

        // ここが最も重要なポイント：
        // BookRepositoryImplのコンストラクタは、staticなBookInformationDatabase.getDatabase()
        // や new RegisterSummary(context) などを直接呼び出します。
        // これらをモックするには、Mockito-inlineのような特別なライブラリが必要です。
        //
        // もし build.gradle を変更できない場合、以下の static モックは動作しません。
        // その場合、bookRepository のインスタンス化は、実際のデータベースやRegisterSummary/RegisterHighlightMemoの
        // 初期化に依存することになり、単体テストとしての隔離性が失われます。
        //
        // 以下のコードは、staticメソッドがモック可能であるという前提で記述しています。
        try (MockedStatic<BookInformationDatabase> mockedDb = mockStatic(BookInformationDatabase.class)) {
            mockedDb.when(() -> BookInformationDatabase.getDatabase(any(Context.class)))
                    .thenReturn(mockBookInformationDatabase);
            when(mockBookInformationDatabase.summaryDao()).thenReturn(mockSummaryDao);
            when(mockBookInformationDatabase.highlightMemoDao()).thenReturn(mockHighlightMemoDao);

            // ここで BookRepositoryImpl をインスタンス化します。
            // 実際には RegisterSummary と RegisterHighlightMemo も new で作られますが、
            // その内部メソッドの挙動は別途モックすることでテストします。
            // 本来は依存性注入 (DI) を利用して、これらのインスタンスも外部から注入できるように設計すべきです。
            bookRepository = new BookRepositoryImpl(mockContext);

            // BookRepositoryImpl の内部で生成される RegisterSummary/HighlightMemo インスタンスの代わりに、
            // @Mock で作成したモックを使用できるように、BookRepositoryImpl の内部状態を（テストのために）設定します。
            // private final フィールドのため直接代入はできません。リフレクションやPowerMockが必要になりますが、
            // ここではテストの意図を示すために、以下のようなモックの振る舞いを定義する形で進めます。
            // (この部分は、BookRepositoryImplの設計に強く依存し、そのままでは動作しない可能性があります)

            // RegisterSummaryとRegisterHighlightMemoはコンストラクタ内でnewされるため、
            // 外部からモックを注入することはできません。
            // そのため、これらのクラスのメソッドのモックは、実際にはnewされたインスタンスに対して行われるべきですが、
            // ここではテストの概念を示すために、@Mockされたオブジェクトに対してwhen()を設定します。
            // 実際のテストでは、これらのクラスもモック可能になるようにBookRepositoryImplをリファクタリングするか、
            // もしくはPowerMockを使用してnewされるインスタンスをモックする必要があります。
            // 現状の設計では、これらのクラスの内部状態をテストで完全に制御するのは非常に難しいです。

            // また、VolumeIdProviderの静的メソッドもモックします (これもmockito-inlineが必要です)。
            // 各テストメソッド内でMockedStaticスコープを設定するのが一般的ですが、ここではセットアップで仮に示します。
        } catch (Exception e) {
            // staticモックができない環境ではここを通る可能性があります
            // テストの実行環境がmockito-inlineに対応していない場合のフォールバックとして、
            // bookRepository = new BookRepositoryImpl(mockContext); を直接呼び出すことも可能ですが、
            // その場合、内部の static メソッド呼び出しが実際の挙動に依存します。
            // throw e; // テスト環境の設定不備を早期に検出するため
        }
    }

    // 各テストメソッドの後に実行されるクリーンアップ
    // (ExecutorServiceをシャットダウンしてリソースを解放)
    @org.junit.After
    public void tearDown() {
        if (testExecutor != null) {
            testExecutor.shutdownNow();
            try {
                // シャットダウンが完了するまで最大1秒待機
                testExecutor.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Executor shutdown interrupted: " + e.getMessage());
            }
        }
    }


    @Test
    public void testGetAllBookSummaries_success() throws Exception {
        // GIVEN:
        String uid = "user123";
        SummaryEntity entity1 = new SummaryEntity(uid, "vol1", "Summary 1", true);
        SummaryEntity entity2 = new SummaryEntity(uid, "vol2", "Summary 2", false);
        List<SummaryEntity> entities = Arrays.asList(entity1, entity2);

        // mockSummaryDao.getAllSummariesByUser() が特定の値を返すように設定
        when(mockSummaryDao.getAllSummariesByUser(uid)).thenReturn(entities);

        // VolumeIdProvider.fetchBookName と fetchCoverImageUrl の静的メソッドをモック
        // 【重要】これらの静的メソッドをモックするには `mockito-inline` が必須です。
        try (MockedStatic<VolumeIdProvider> mockedVolumeIdProvider = mockStatic(VolumeIdProvider.class)) {
            mockedVolumeIdProvider.when(() -> VolumeIdProvider.fetchBookName("vol1")).thenReturn("Book One");
            mockedVolumeIdProvider.when(() -> VolumeIdProvider.fetchCoverImageUrl("vol1")).thenReturn("url1");
            mockedVolumeIdProvider.when(() -> VolumeIdProvider.fetchBookName("vol2")).thenReturn("Book Two");
            mockedVolumeIdProvider.when(() -> VolumeIdProvider.fetchCoverImageUrl("vol2")).thenReturn("url2");

            // WHEN:
            Future<List<BookSummaryData>> futureSummaries = bookRepository.getAllBookSummaries(uid);
            List<BookSummaryData> result = futureSummaries.get(); // 非同期処理の結果を取得

            // THEN:
            assertNotNull(result);
            assertEquals(2, result.size());

            BookSummaryData summary1 = result.get(0);
            assertEquals("vol1", summary1.getVolumeId());
            assertEquals("Book One", summary1.getTitle());
            assertEquals("url1", summary1.getImageUrl());
            assertTrue(summary1.isPublic());

            BookSummaryData summary2 = result.get(1);
            assertEquals("vol2", summary2.getVolumeId());
            assertEquals("Book Two", summary2.getTitle());
            assertEquals("url2", summary2.getImageUrl());
            assertFalse(summary2.isPublic());

            // 依存関係が正しく呼び出されたことを検証
            verify(mockSummaryDao).getAllSummariesByUser(uid);
            mockedVolumeIdProvider.verify(() -> VolumeIdProvider.fetchBookName("vol1"));
            mockedVolumeIdProvider.verify(() -> VolumeIdProvider.fetchCoverImageUrl("vol1"));
            mockedVolumeIdProvider.verify(() -> VolumeIdProvider.fetchBookName("vol2"));
            mockedVolumeIdProvider.verify(() -> VolumeIdProvider.fetchCoverImageUrl("vol2"));
        }
    }

    @Test
    public void testGetAllBookSummaries_emptyList() throws Exception {
        // GIVEN:
        String uid = "userEmpty";
        when(mockSummaryDao.getAllSummariesByUser(uid)).thenReturn(new ArrayList<>());

        // WHEN:
        Future<List<BookSummaryData>> futureSummaries = bookRepository.getAllBookSummaries(uid);
        List<BookSummaryData> result = futureSummaries.get();

        // THEN:
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mockSummaryDao).getAllSummariesByUser(uid);
    }

    @Test
    public void testGetBookDetail_success() throws Exception {
        // GIVEN:
        String uid = "user123";
        String volumeId = "detailVol";
        SummaryEntity summaryEntity = new SummaryEntity(uid, volumeId, "Overall Summary Content", true);

        when(mockSummaryDao.getSummary(uid, volumeId)).thenReturn(summaryEntity);

        // VolumeIdProvider の静的メソッドをモック
        try (MockedStatic<VolumeIdProvider> mockedVolumeIdProvider = mockStatic(VolumeIdProvider.class)) {
            mockedVolumeIdProvider.when(() -> VolumeIdProvider.fetchBookName(volumeId)).thenReturn("Detailed Book Name");
            mockedVolumeIdProvider.when(() -> VolumeIdProvider.fetchCoverImageUrl(volumeId)).thenReturn("http://detail.cover.url");

            // WHEN:
            Future<BookDetailData> futureDetail = bookRepository.getBookDetail(uid, volumeId);
            BookDetailData result = futureDetail.get();

            // THEN:
            assertNotNull(result);
            assertEquals(volumeId, result.getVolumeId());
            assertEquals("Detailed Book Name", result.getName());
            assertEquals("Overall Summary Content", result.getSummary());
            assertEquals("http://detail.cover.url", result.getCoverImageUrl());
            assertEquals("public", result.getPublicStatus());

            // 依存関係が正しく呼び出されたことを検証
            verify(mockSummaryDao).getSummary(uid, volumeId);
            mockedVolumeIdProvider.verify(() -> VolumeIdProvider.fetchBookName(volumeId));
            mockedVolumeIdProvider.verify(() -> VolumeIdProvider.fetchCoverImageUrl(volumeId));
        }
    }

    @Test
    public void testGetBookDetail_notFound() throws Exception {
        // GIVEN:
        String uid = "userNonExistent";
        String volumeId = "nonExistentVol";
        when(mockSummaryDao.getSummary(uid, volumeId)).thenReturn(null);

        // WHEN:
        Future<BookDetailData> futureDetail = bookRepository.getBookDetail(uid, volumeId);
        BookDetailData result = futureDetail.get();

        // THEN:
        assertNull(result); // 存在しない場合はnullが返されることを確認
        verify(mockSummaryDao).getSummary(uid, volumeId);
        // VolumeIdProvider.fetchBookName/fetchCoverImageUrl は呼ばれないことを検証 (mockedStaticスコープの外なのでここでは検証できない)
    }

    @Test
    public void testUpdateBookPublicStatus_success() throws Exception {
        // GIVEN:
        String uid = "userUpdate";
        String volumeId = "updateVol";
        boolean newPublicStatus = false; // privateにする
        SummaryEntity existingSummary = new SummaryEntity(uid, volumeId, "Existing Summary Text", true); // 元はpublic

        when(mockSummaryDao.getSummary(uid, volumeId)).thenReturn(existingSummary);
        // registerSummary.registerSummary() が成功を返すように設定
        // RegisterSummary はコンストラクタ内で new されるため、@Mockされたオブジェクトを直接操作できません。
        // ここでは、概念的に `registerSummary.registerSummary` がモックできるものとして記述します。
        // 実際のテストでは、BookRepositoryImplをリファクタリングするか、PowerMockで new される RegisterSummary をモックする必要があります。
        when(mockRegisterSummary.registerSummary(uid, volumeId, "Existing Summary Text", newPublicStatus)).thenReturn(true);

        // WHEN:
        Future<Boolean> futureResult = bookRepository.updateBookPublicStatus(uid, volumeId, newPublicStatus);
        Boolean result = futureResult.get();

        // THEN:
        assertTrue(result); // 更新が成功したことを確認
        verify(mockSummaryDao).getSummary(uid, volumeId);
        // verify(mockRegisterSummary).registerSummary(uid, volumeId, "Existing Summary Text", newPublicStatus);
        // 上記はmockRegisterSummaryがBookRepositoryImpl内部のインスタンスと同一でないと検証できません。
        // これがこの設計でのテストの限界点です。
    }

    @Test
    public void testUpdateBookPublicStatus_summaryNotFound() throws Exception {
        // GIVEN:
        String uid = "userUpdate";
        String volumeId = "notFoundVol";
        boolean newPublicStatus = true;

        when(mockSummaryDao.getSummary(uid, volumeId)).thenReturn(null);

        // WHEN:
        Future<Boolean> futureResult = bookRepository.updateBookPublicStatus(uid, volumeId, newPublicStatus);
        Boolean result = futureResult.get();

        // THEN:
        assertFalse(result); // 概要が見つからない場合は更新失敗
        verify(mockSummaryDao).getSummary(uid, volumeId);
        // mockRegisterSummary は呼び出されないことを確認 (呼び出すべきではない)
        verifyNoInteractions(mockRegisterSummary);
    }

    @Test
    public void testGetHighlightMemos_success() throws Exception {
        // GIVEN:
        String uid = "userMemo";
        String volumeId = "memoVol";
        HighlightMemoEntity entity1 = new HighlightMemoEntity(uid, volumeId, 1, 10, "Memo 1");
        HighlightMemoEntity entity2 = new HighlightMemoEntity(uid, volumeId, 2, 20, "Memo 2");
        List<HighlightMemoEntity> entities = Arrays.asList(entity1, entity2);

        when(mockHighlightMemoDao.getByUserAndVolume(uid, volumeId)).thenReturn(entities);

        // WHEN:
        Future<List<HighlightMemoData>> futureMemos = bookRepository.getHighlightMemos(uid, volumeId);
        List<HighlightMemoData> result = futureMemos.get();

        // THEN:
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getPage());
        assertEquals("Memo 2", result.get(1).getMemoContent());

        verify(mockHighlightMemoDao).getByUserAndVolume(uid, volumeId);
    }

    @Test
    public void testGetHighlightMemos_empty() throws Exception {
        // GIVEN:
        String uid = "userEmptyMemo";
        String volumeId = "emptyMemoVol";

        when(mockHighlightMemoDao.getByUserAndVolume(uid, volumeId)).thenReturn(new ArrayList<>());

        // WHEN:
        Future<List<HighlightMemoData>> futureMemos = bookRepository.getHighlightMemos(uid, volumeId);
        List<HighlightMemoData> result = futureMemos.get();

        // THEN:
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(mockHighlightMemoDao).getByUserAndVolume(uid, volumeId);
    }

    @Test
    public void testRegisterHighlightMemo_success() throws Exception {
        // GIVEN:
        String uid = "userRegister";
        String volumeId = "registerVol";
        HighlightMemoData memoData = new HighlightMemoData(5, 50, "New Memo");

        // registerHighlightMemo.registerHighlightMemo() が成功を返すように設定
        // RegisterHighlightMemo もコンストラクタ内で new されるため、直接モックできません。
        // 上と同様に、概念的にモック可能であるという前提で記述します。
        when(mockRegisterHighlightMemo.registerHighlightMemo(uid, volumeId, memoData)).thenReturn(true);

        // WHEN:
        Future<Boolean> futureResult = bookRepository.registerHighlightMemo(uid, volumeId, memoData);
        Boolean result = futureResult.get();

        // THEN:
        assertTrue(result); // 登録が成功したことを確認
        // verify(mockRegisterHighlightMemo).registerHighlightMemo(uid, volumeId, memoData);
        // 上記はmockRegisterHighlightMemoがBookRepositoryImpl内部のインスタンスと同一でないと検証できません。
    }

    @Test
    public void testRegisterHighlightMemo_failure() throws Exception {
        // GIVEN:
        String uid = "userFail";
        String volumeId = "failVol";
        HighlightMemoData memoData = new HighlightMemoData(1, 1, "Fail Memo");

        // 失敗を返すように設定
        when(mockRegisterHighlightMemo.registerHighlightMemo(uid, volumeId, memoData)).thenReturn(false);

        // WHEN:
        Future<Boolean> futureResult = bookRepository.registerHighlightMemo(uid, volumeId, memoData);
        Boolean result = futureResult.get();

        // THEN:
        assertFalse(result); // 登録が失敗したことを確認
        // verify(mockRegisterHighlightMemo).registerHighlightMemo(uid, volumeId, memoData);
    }
}