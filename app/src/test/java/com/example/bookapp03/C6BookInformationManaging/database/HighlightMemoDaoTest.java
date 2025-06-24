package com.example.bookapp03.C6BookInformationManaging.database;

import static org.junit.Assert.*;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

/**
 * HighlightMemoDao の単体テスト
 * - ブラックボックス: insert/getByUserAndVolume/delete/deleteAll の動作検証
 * - ホワイトボックス: リストサイズや戻り値を検証
 */
@RunWith(RobolectricTestRunner.class)
public class HighlightMemoDaoTest {

    private BookInformationDatabase db;
    private HighlightMemoDao dao;

    @Before
    public void setUp() {
        db = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                BookInformationDatabase.class
        ).allowMainThreadQueries().build();
        dao = db.highlightMemoDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testInsertAndGetByUserAndVolume() {
        HighlightMemoEntity e1 = new HighlightMemoEntity("uidA", "volA", 1, 1, "memo1");
        HighlightMemoEntity e2 = new HighlightMemoEntity("uidA", "volA", 2, 2, "memo2");
        dao.insert(e1);
        dao.insert(e2);

        List<HighlightMemoEntity> list = dao.getByUserAndVolume("uidA", "volA");
        assertNotNull(list);
        assertEquals(2, list.size());
    }

    @Test
    public void testDeleteEntity() {
        HighlightMemoEntity entity = new HighlightMemoEntity("uidB", "volB", 3, 3, "memo3");
        long id = dao.insert(entity);
        // id は無視して delete(entity) が 1 を返す
        int result = dao.delete(new HighlightMemoEntity("uidB", "volB", 3, 3, "memo3"));
        assertEquals(1, result);
    }

    @Test
    public void testDeleteAll() {
        dao.insert(new HighlightMemoEntity("uidC", "volC", 1, 1, "m"));
        dao.insert(new HighlightMemoEntity("uidC", "volC", 2, 2, "n"));

        int count = dao.deleteAll("uidC", "volC");
        assertEquals(2, count);

        List<HighlightMemoEntity> empty = dao.getByUserAndVolume("uidC", "volC");
        assertTrue(empty.isEmpty());
    }
}