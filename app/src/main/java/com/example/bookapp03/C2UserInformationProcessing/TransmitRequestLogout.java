package com.example.bookapp03.C2UserInformationProcessing;

import android.app.Activity;

import com.example.bookapp03.C5UserInformationManaging.Logout;

/**
 * モジュール名: ログアウト要求の受け渡し
 * 作成者: 鶴田凌
 * 作成日: 2025/06/16
 * 概要: UI層からのログアウト要求を受け取り、
 * 実際のログアウト処理クラス(Logout)へ委譲するクラス
 * 履歴:
 * 2025/06/16 鶴田凌 新規作成
 */
public class TransmitRequestLogout {
    private final Activity activity;

    /**
     * コンストラクタ
     *
     * @param activity 呼び出し元のActivity
     */
    public TransmitRequestLogout(Activity activity) {
        this.activity = activity;
    }

    /**
     * ログアウト処理を実行する。
     *
     * @return true=処理成功、false=処理失敗
     */
    public boolean transmitRequestLogout() {
        Logout logout = new Logout(activity);
        return logout.logout();
    }
}
