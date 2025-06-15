package com.example.bookapp03.C3BookInformationProcessing;

import android.content.Context;

import com.example.bookapp03.C1UIProcessing.HighlightMemoData;
import com.example.bookapp03.C6BookInformationManaging.RegisterHighlightMemo;

/**
 * モジュール名: ハイライト・メモ登録情報の受け渡し
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ハイライトメモのデータを C6BookInformationManaging.RegisterHighlightMemoに送信し、ローカル DB 登録を実行するクラス
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
public class TransmitHighlightMemo {
    /**
     * アプリケーションコンテキスト
     */
    private final Context context;
    /**
     * ユーザ ID
     */
    private final String uid;
    /**
     * 書籍ボリューム ID
     */
    private final String volumeId;

    /**
     * コンストラクタ。
     *
     * @param context  呼び出し元のコンテキスト
     * @param uid      ユーザ ID
     * @param volumeId 書籍ボリューム ID
     */
    public TransmitHighlightMemo(Context context, String uid, String volumeId) {
        this.context = context.getApplicationContext();
        this.uid = uid;
        this.volumeId = volumeId;
    }

    /**
     * 指定された HighlightMemoData をローカルデータベースに登録する。
     *
     * @param data 登録対象のハイライトメモデータ (ページ、行、メモ)
     * @return true = 登録成功、false = 登録失敗
     */
    public boolean transmitHighlightMemo(HighlightMemoData data) {
        RegisterHighlightMemo register =
                new RegisterHighlightMemo(context);
        return register.registerHighlightMemo(uid, volumeId, data);
    }
}
