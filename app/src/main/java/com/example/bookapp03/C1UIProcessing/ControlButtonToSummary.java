package com.example.bookapp03.C1UIProcessing;

import android.content.Context;
import android.content.Intent;

/**
 * モジュール名: 全体まとめ登録画面への遷移ボタン制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: 全体まとめ登録画面へ遷移するボタンを制御するクラス
 * 履歴:
 * 　2025/06/15 鶴田凌 新規作成
 */
public class ControlButtonToSummary {

    /**
     * 全体まとめ登録画面への遷移を実行する。
     *
     * @param context ボタン押下時のコンテキスト（Activity 等）
     */
    public void setToSummary(Context context) {
        Intent intent = new Intent(context, DisplaySummary.class);
        context.startActivity(intent);
    }
}
