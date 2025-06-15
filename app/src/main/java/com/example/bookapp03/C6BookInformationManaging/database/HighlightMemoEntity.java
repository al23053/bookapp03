package com.example.bookapp03.C6BookInformationManaging.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * モジュール名: ハイライトメモエンティティ
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ユーザ／書籍ごとのハイライトメモ（ページ・行・メモ）を表す Room のエンティティ
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
@Entity(tableName = "highlight_memo")
public class HighlightMemoEntity {
    /**
     * レコード識別用の自動採番 ID
     */
    @PrimaryKey(autoGenerate = true)
    public long id;

    /**
     * Firebase Authentication で取得したユーザ UID
     */
    public String uid;

    /**
     * Google Books API で取得した書籍のボリュームID
     */
    public String volumeId;

    /**
     * ハイライト対象のページ番号
     */
    public int page;

    /**
     * ハイライト対象の行数
     */
    public int line;

    /**
     * ハイライトメモの文字列
     */
    public String memo;

    /**
     * Room 用引数付きコンストラクタ
     *
     * @param uid      ユーザ UID
     * @param volumeId 書籍ボリュームID
     * @param page     ページ番号
     * @param line     行数
     * @param memo     メモ文字列
     */
    public HighlightMemoEntity(
            String uid,
            String volumeId,
            int page,
            int line,
            String memo
    ) {
        this.uid = uid;
        this.volumeId = volumeId;
        this.page = page;
        this.line = line;
        this.memo = memo;
    }
}
