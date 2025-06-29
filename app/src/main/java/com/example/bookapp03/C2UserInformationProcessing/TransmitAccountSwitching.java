package com.example.bookapp03.C2UserInformationProcessing;

import android.app.Activity;

import com.example.bookapp03.C5UserInformationManaging.RegisterAccountSwitching;

/**
 * モジュール名: アカウント切替情報の受け渡し
 * 作成者: 鶴田凌
 * 作成日: 2025/06/16
 * 概要: ControlAccountSwitching から受け取った UserInfoをRegisterAccountSwitching に渡すクラス
 * 履歴:
 * 2025/06/16 鶴田凌 新規作成
 */
public class TransmitAccountSwitching {
    private final Activity activity;

    public TransmitAccountSwitching(Activity activity) {
        this.activity = activity;
    }

    /**
     * @param info 切替先のユーザ情報
     * @return true=切替処理成功 / false=失敗
     */
    public boolean transmitAccountSwitching(UserInfo info) {
        RegisterAccountSwitching reg =
                new RegisterAccountSwitching(activity);
        return reg.registerAccountSwitching(info);
    }
}
