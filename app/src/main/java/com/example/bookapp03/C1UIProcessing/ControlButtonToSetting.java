package com.example.bookapp03.C1UIProcessing;

import android.content.Context;
import android.content.Intent;

/**
 * モジュール名: ユーザ設定画面に遷移するボタン制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/16
 * 概要: ユーザ設定画面へ遷移するボタンを制御するクラス
 * 履歴:
 * 　2025/06/16 鶴田凌 新規作成
 */
public class ControlButtonToSetting {

    /**
     * ユーザ設定画面への遷移を実行する。
     *
     * @param context ボタン押下時のコンテキスト（Activity 等）
     */
    public void setToSetting(Context context) {
        Intent intent = new Intent(context, DisplaySetting.class);
        context.startActivity(intent);
    }
}
