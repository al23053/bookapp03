package com.example.bookapp03.C6BookInformationManaging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.example.bookapp03.C1UIProcessing.HighlightMemoData;
import com.example.bookapp03.C6BookInformationManaging.database.BookInformationDatabase;
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * RegisterHighlightMemoの単体テスト
 * - DAO insert 成功／失敗で戻り値を検証
 * - insert 呼び出しパラメータを検証
 */
public class RegisterHighlightMemoTest {
    private Context mockContext;
    private HighlightMemoDao mockDao;
    private BookInformationDatabase mockDb;
    private MockedStatic<BookInformationDatabase> dbStatic;

    @Before
    public void setUp() {
        mockContext = mock(Context.class);
        mockDao     = mock(HighlightMemoDao.class);
        mockDb      = mock(BookInformationDatabase.class);

        dbStatic = Mockito.mockStatic(BookInformationDatabase.class);
        dbStatic.when(() -> BookInformationDatabase.getDatabase(mockContext))
                .thenReturn(mockDb);
        when(mockDb.highlightMemoDao()).thenReturn(mockDao);
    }

    @After
    public void tearDown() {
        dbStatic.close();
    }

    @Test
    public void testRegisterHighlightMemo_successfulInsert_returnsTrue() throws Exception {
        HighlightMemoData data = new HighlightMemoData(1,2,"m");
        when(mockDao.insert(eq(new com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoEntity(
            "u","v",1,2,"m")))).thenReturn(5L);

        RegisterHighlightMemo sut =
            new RegisterHighlightMemo(mockContext);
        boolean result = sut.registerHighlightMemo("u","v", data);

        assertTrue(result);
        verify(mockDao).insert(eq(new com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoEntity(
            "u","v",1,2,"m")));
    }

    @Test
    public void testRegisterHighlightMemo_failedInsert_returnsFalse() throws Exception {
        HighlightMemoData data = new HighlightMemoData(1,2,"m");
        when(mockDao.insert(eq(new com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoEntity(
            "u","v",1,2,"m")))).thenReturn(-1L);

        RegisterHighlightMemo sut =
            new RegisterHighlightMemo(mockContext);
        boolean result = sut.registerHighlightMemo("u","v", data);

        assertFalse(result);
    }
}