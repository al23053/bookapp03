package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

import com.example.bookapp03.C2UserInformationProcessing.TransmitRequestLogout;

/**
 * モジュール名: ログアウト処理
 * 作成者: 鶴田凌
 * 作成日: 2025/06/16
 * 概要: ログアウトを確認し、承認されたら FirebaseAuth でサインアウトし、ログイン画面へ遷移するクラス
 * 履歴:
 * 2025/06/16 鶴田凌 新規作成
 */
public class LogoutProcessing {
    private final Activity activity;

    /**
     * コンストラクタ
     *
     * @param activity 呼び出し元の Activity
     */
    public LogoutProcessing(Activity activity) {
        this.activity = activity;
    }

    /**
     * ログアウト確認ダイアログを表示し、「はい」が押されたらサインアウトして
     * ログイン画面へ遷移する。
     * （ログイン画面クラスは DisplayLogin と仮定）
     */
    public void confirmLogout() {
        new AlertDialog.Builder(activity)
                .setTitle("ログアウト")
                .setMessage("ログアウトしますか？")
                .setPositiveButton("はい", (dlg, which) -> {
                    // ログアウト要求をTransmitRequestLogoutへ転送
                    new TransmitRequestLogout(activity).transmitRequestLogout();
                })
                .setNegativeButton("いいえ", null)
                .show();
    }
}
