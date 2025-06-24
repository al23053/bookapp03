package com.example.bookapp03.C6BookInformationManaging.database;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * BookInformationDatabase の単体テスト
 * - ブラックボックス: getDatabase でインスタンスが取得できること
 * - ホワイトボックス: DAO が non-null であること、シングルトン性を検証
 */
@RunWith(RobolectricTestRunner.class)
public class BookInformationDatabaseTest {

    @Test
    public void testGetDatabaseSingletonAndDaosNotNull() {
        Context ctx = ApplicationProvider.getApplicationContext();
        BookInformationDatabase db1 = BookInformationDatabase.getDatabase(ctx);
        BookInformationDatabase db2 = BookInformationDatabase.getDatabase(ctx);

        // シングルトン: 同一インスタンスが返る
        assertSame(db1, db2);

        // DAO が取得できる
        assertNotNull(db1.highlightMemoDao());
        assertNotNull(db1.summaryDao());
    }
}