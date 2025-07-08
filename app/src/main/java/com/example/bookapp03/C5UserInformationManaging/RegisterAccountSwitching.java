package com.example.bookapp03.C5UserInformationManaging;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.bookapp03.C2UserInformationProcessing.UserInfo;

/**
 * モジュール名: アカウント切替保存
 * 作成者: 鶴田凌
 * 作成日: 2025/06/16
 * 概要: 切替先のユーザ情報を SharedPreferences に保存し、必要なら Firestore にも更新を行うクラス
 * 履歴:
 * 2025/06/16 鶴田凌 新規作成
 */
public class RegisterAccountSwitching {
    private static final String PREFS = "app_prefs";
    private final Context context;

    public RegisterAccountSwitching(Activity activity) {
        this.context = activity.getApplicationContext();
    }

    /**
     * @param info UserInfo オブジェクト
     * @return true=SharedPreferences への保存成功
     */
    public boolean registerAccountSwitching(UserInfo info) {
        try {
            SharedPreferences sp =
                    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = sp.edit();
            ed.putString("current_uid", info.uid);
            ed.putString("current_nickname", info.nickname);
            ed.putString("current_email", info.email);
            ed.apply();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
