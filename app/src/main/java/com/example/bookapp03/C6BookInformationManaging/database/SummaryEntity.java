package com.example.bookapp03.C6BookInformationManaging.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;

/**
 * モジュール名: 全体まとめ情報エンティティ
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ユーザ／書籍ごとの全体まとめ（要約）を表す Room のエンティティ
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
@Entity(tableName = "summary", primaryKeys = {"uid", "volumeId"})
public class SummaryEntity {

    /** Firebase Authentication で取得したユーザ UID */
    @NonNull
    public String uid;

    /** Google Books API で取得した書籍のボリュームID */
    @NonNull
    public String volumeId;

    /** 全体まとめ（要約）テキスト。未設定時は null */
    @Nullable
    public String overallSummary;

    /** 要約の公開フラグ */
    public boolean isPublic;

    /**
     * Room 用デフォルトコンストラクタ
     */
    public SummaryEntity() {
        // Room が使用
    }

    /**
     * アプリケーション用コンストラクタ
     *
     * @param uid            ユーザ UID
     * @param volumeId       書籍ボリュームID
     * @param overallSummary 要約テキスト
     * @param isPublic       公開フラグ
     */
    @Ignore
    public SummaryEntity(@NonNull String uid, @NonNull String volumeId,
                         @Nullable String overallSummary,
                         boolean isPublic) {
        this.uid = uid;
        this.volumeId = volumeId;
        this.overallSummary = overallSummary;
        this.isPublic = isPublic;
    }
}
