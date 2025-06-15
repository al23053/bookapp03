package com.example.bookapp03.C6BookInformationManaging.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * モジュール名: ハイライトメモ DAO
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ハイライトメモ（HighlightMemoEntity）の取得・挿入・削除を行う DAO
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
@Dao
public interface HighlightMemoDao {

    /**
     * 指定のユーザ／書籍のハイライトメモをリストで取得する。
     *
     * @param uid      ユーザ UID
     * @param volumeId 書籍ボリュームID
     * @return HighlightMemoEntity のリスト
     */
    @Query("SELECT * FROM highlight_memo WHERE uid=:uid AND volumeId=:volumeId")
    List<HighlightMemoEntity> getByUserAndVolume(String uid, String volumeId);

    /**
     * ハイライトメモを挿入する。
     *
     * @param entity 挿入対象の HighlightMemoEntity
     * @return 挿入した行 ID
     */
    @Insert
    long insert(HighlightMemoEntity entity);

    /**
     * ハイライトメモを削除する。
     *
     * @param entity 削除対象の HighlightMemoEntity
     * @return 削除した行数
     */
    @Delete
    int delete(HighlightMemoEntity entity);

    /**
     * 指定のユーザ／書籍の全ハイライトメモを削除する。
     *
     * @param uid      ユーザ UID
     * @param volumeId 書籍ボリュームID
     * @return 削除した行数
     */
    @Query("DELETE FROM highlight_memo WHERE uid=:uid AND volumeId=:volumeId")
    int deleteAll(String uid, String volumeId);
}
