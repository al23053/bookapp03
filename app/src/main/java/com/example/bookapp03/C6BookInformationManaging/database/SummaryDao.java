package com.example.bookapp03.C6BookInformationManaging.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

/**
 * モジュール名: 全体まとめ DAO
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: 全体まとめ（SummaryEntity）の取得・登録・更新・削除を行う DAO
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
@Dao
public interface SummaryDao {

    /**
     * 指定のユーザ／書籍の全体まとめ情報を取得する。
     *
     * @param uid      ユーザ UID
     * @param volumeId 書籍ボリュームID
     * @return SummaryEntity オブジェクト（未登録なら null）
     */
    @Query("SELECT * FROM summary WHERE uid=:uid AND volumeId=:volumeId")
    SummaryEntity getSummary(String uid, String volumeId);

    /**
     * 全体まとめ情報を挿入または置き換える（onConflict = REPLACE）。
     *
     * @param entity 挿入または更新対象の SummaryEntity
     * @return 挿入後の行 ID（主キー）
     */
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    long insert(SummaryEntity entity);

    /**
     * 全体まとめ情報を更新する。
     *
     * @param entity 更新対象の SummaryEntity
     * @return 更新した行数
     */
    @Update
    int update(SummaryEntity entity);

    /**
     * 指定のユーザ／書籍の全体まとめ情報を削除する。
     *
     * @param uid      ユーザ UID
     * @param volumeId 書籍ボリュームID
     * @return 削除した行数
     */
    @Query("DELETE FROM summary WHERE uid=:uid AND volumeId=:volumeId")
    int delete(String uid, String volumeId);
}
