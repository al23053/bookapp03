package com.example.bookapp03.C6BookInformationManaging.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * モジュール名: 書籍情報データベース
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: Room を用いてハイライトメモ(HighlightMemoEntity)と全体まとめ(SummaryEntity)のテーブルを管理するデータベース
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
@Database(
        entities = {
                HighlightMemoEntity.class,
                SummaryEntity.class
        },
        version = 1,
        exportSchema = false
)
public abstract class BookInformationDatabase extends RoomDatabase {
    private static final String DB_NAME = "bookapp03.db";
    private static volatile BookInformationDatabase instance;

    /**
     * ハイライトメモ用 DAO を取得する。
     *
     * @return HighlightMemoDao の実装
     */
    public abstract HighlightMemoDao highlightMemoDao();

    /**
     * 全体まとめ用 DAO を取得する。
     *
     * @return SummaryDao の実装
     */
    public abstract SummaryDao summaryDao();

    /**
     * シングルトンでデータベースインスタンスを取得する。
     *
     * @param ctx アプリケーションコンテキスト
     * @return データベースのシングルトンインスタンス
     */
    public static BookInformationDatabase getDatabase(Context ctx) {
        if (instance == null) {
            synchronized (BookInformationDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            ctx.getApplicationContext(),
                            BookInformationDatabase.class,
                            DB_NAME
                    ).build();
                }
            }
        }
        return instance;
    }
}
