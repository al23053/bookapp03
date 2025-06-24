package com.example.bookapp03.C6BookInformationManaging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.bookapp03.C6BookInformationManaging.database.BookInformationDatabase;
import com.example.bookapp03.C6BookInformationManaging.database.SummaryDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * RegisterSummary の単体テスト
 * - ローカル登録成功・失敗時の戻り値検証
 * - 公開フラグ ON/OFF 時の Firestore 呼び出し有無検証
 */
public class RegisterSummaryTest {
    private Context mockContext;
    private SummaryDao mockDao;
    private FirebaseFirestore mockFs;
    private BookInformationDatabase mockDb;
    private MockedStatic<BookInformationDatabase> dbStatic;
    private MockedStatic<FirebaseFirestore> fsStatic;

    @Before
    public void setUp() {
        mockContext = mock(Context.class);
        mockDao     = mock(SummaryDao.class);
        mockDb      = mock(BookInformationDatabase.class);
        mockFs      = mock(FirebaseFirestore.class);

        dbStatic = Mockito.mockStatic(BookInformationDatabase.class);
        fsStatic = Mockito.mockStatic(FirebaseFirestore.class);

        dbStatic.when(() -> BookInformationDatabase.getDatabase(mockContext))
                .thenReturn(mockDb);
        when(mockDb.summaryDao()).thenReturn(mockDao);

        fsStatic.when(FirebaseFirestore::getInstance)
                .thenReturn(mockFs);
    }

    @After
    public void tearDown() {
        dbStatic.close();
        fsStatic.close();
    }

    @Test
    public void testRegisterSummary_localSuccess_publicTrue_callsFirestore() throws Exception {
        when(mockDao.insert(eq(new SummaryEntity("u","v","s", true))))
            .thenReturn(1L);
        CollectionReference mockCol = mock(CollectionReference.class);
        DocumentReference mockDoc = mock(DocumentReference.class);
        when(mockFs.collection("summaries")).thenReturn(mockCol);
        when(mockCol.document("u_v")).thenReturn(mockDoc);

        RegisterSummary sut = new RegisterSummary(mockContext);
        boolean result = sut.registerSummary("u","v","s", true);

        assertTrue(result);
        verify(mockDao).insert(eq(new SummaryEntity("u","v","s", true)));
        verify(mockDoc).set(eq(new java.util.HashMap<String,Object>() {{
            put("uid","u"); put("volumeId","v");
            put("overallSummary","s"); put("isPublic", true);
        }}));
    }

    @Test
    public void testRegisterSummary_localSuccess_publicFalse_noFirestore() throws Exception {
        when(mockDao.insert(eq(new SummaryEntity("u","v","s", false))))
            .thenReturn(1L);

        RegisterSummary sut = new RegisterSummary(mockContext);
        boolean result = sut.registerSummary("u","v","s", false);

        assertTrue(result);
        verify(mockDao).insert(eq(new SummaryEntity("u","v","s", false)));
        verify(mockFs, Mockito.never()).collection("summaries");
    }

    @Test
    public void testRegisterSummary_localFail_returnsFalse() throws Exception {
        when(mockDao.insert(eq(new SummaryEntity("u","v","s", true))))
            .thenReturn(-1L);

        RegisterSummary sut = new RegisterSummary(mockContext);
        boolean result = sut.registerSummary("u","v","s", true);

        assertFalse(result);
        verify(mockFs, Mockito.never()).collection("summaries");
    }
}