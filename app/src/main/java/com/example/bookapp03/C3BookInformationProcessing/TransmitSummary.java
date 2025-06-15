package com.example.bookapp03.C3BookInformationProcessing;

import android.content.Context;

import com.example.bookapp03.C6BookInformationManaging.RegisterSummary;

/**
 * モジュール名: 全体まとめ登録内容の受け渡し
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: 全体まとめのデータを C6BookInformationManaging.RegisterSummaryに送信し、登録処理を行うクラス
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
public class TransmitSummary {
    /**
     * アプリケーションコンテキスト
     */
    private final Context context;

    /**
     * コンストラクタ。
     *
     * @param context 呼び出し元のコンテキスト
     */
    public TransmitSummary(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * 全体まとめデータを RegisterSummary に渡して登録を実行する。
     *
     * @param uid            ユーザ ID
     * @param volumeId       書籍ボリューム ID
     * @param overallSummary 要約テキスト
     * @param isPublic       公開フラグ
     * @return true = 登録成功、false = 登録失敗
     */
    public boolean transmitSummary(
            String uid,
            String volumeId,
            String overallSummary,
            boolean isPublic
    ) {
        RegisterSummary reg = new RegisterSummary(context);
        return reg.registerSummary(uid, volumeId, overallSummary, isPublic);
    }
}
