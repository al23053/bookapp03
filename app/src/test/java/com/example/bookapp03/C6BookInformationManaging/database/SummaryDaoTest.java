package com.example.bookapp03.C6BookInformationManaging.database;

import static org.junit.Assert.*;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * SummaryDao の単体テスト
 * - ブラックボックス: insert/get/update/delete の動作検証
 * - ホワイトボックス: SQL クエリ結果と戻り値を検証
 */
@RunWith(RobolectricTestRunner.class)
public class SummaryDaoTest {

    private BookInformationDatabase db;
    private SummaryDao dao;

    @Before
    public void setUp() {
        db = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                BookInformationDatabase.class
        ).allowMainThreadQueries().build();
        dao = db.summaryDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testInsertAndGetSummary() {
        SummaryEntity entity = new SummaryEntity("uid1", "vol1", "summary text", true);
        long rowId = dao.insert(entity);
        assertTrue(rowId >= 0);

        SummaryEntity loaded = dao.getSummary("uid1", "vol1");
        assertNotNull(loaded);
        assertEquals("uid1", loaded.uid);
        assertEquals("vol1", loaded.volumeId);
        assertEquals("summary text", loaded.overallSummary);
        assertTrue(loaded.isPublic);
    }

    @Test
    public void testUpdateSummary() {
        SummaryEntity entity = new SummaryEntity("uid2", "vol2", "initial", false);
        dao.insert(entity);

        entity.overallSummary = "updated";
        entity.isPublic = true;
        int count = dao.update(entity);
        assertEquals(1, count);

        SummaryEntity updated = dao.getSummary("uid2", "vol2");
        assertEquals("updated", updated.overallSummary);
        assertTrue(updated.isPublic);
    }

    @Test
    public void testDeleteSummary() {
        SummaryEntity entity = new SummaryEntity("uid3", "vol3", null, false);
        dao.insert(entity);

        int deleted = dao.delete("uid3", "vol3");
        assertEquals(1, deleted);

        SummaryEntity loaded = dao.getSummary("uid3", "vol3");
        assertNull(loaded);
    }
}