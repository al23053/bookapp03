package com.example.bookapp03.C6BookInformationManaging.database;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * モジュール名: 全体まとめ情報エンティティ
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ユーザ／書籍ごとの全体まとめ（要約）を表す Room のエンティティ
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
@Entity(tableName = "summary")
public class SummaryEntity {
    /**
     * Firebase Authentication で取得したユーザ UID
     */
    @PrimaryKey
    public String uid;

    /**
     * Google Books API で取得した書籍のボリュームID
     */
    @PrimaryKey
    public String volumeId;

    /**
     * 全体まとめ（要約）テキスト。未設定時は null
     */
    @Nullable
    public String overallSummary;

    /**
     * 要約の公開フラグ
     */
    public boolean isPublic;

    /**
     * 空コンストラクタ（Room 用）
     */
    public SummaryEntity() { /* Room が使用 */ }

    /**
     * コンストラクタ
     *
     * @param uid            ユーザ UID
     * @param volumeId       書籍ボリュームID
     * @param overallSummary 要約テキスト
     * @param isPublic       公開フラグ
     */
    public SummaryEntity(String uid, String volumeId,
                         @Nullable String overallSummary,
                         boolean isPublic) {
        this.uid = uid;
        this.volumeId = volumeId;
        this.overallSummary = overallSummary;
        this.isPublic = isPublic;
    }
}
