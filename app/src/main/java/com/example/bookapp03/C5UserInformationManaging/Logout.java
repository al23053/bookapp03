package com.example.bookapp03.C5UserInformationManaging;

import android.app.Activity;
import android.content.Intent;

import com.example.bookapp03.C1UIProcessing.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;


/**
 * モジュール名: ログアウト
 * 作成者: 鶴田凌
 * 作成日: 2025/06/16
 * 概要: FirebaseAuth を用いてサインアウトし、
 * ログイン画面(DisplayLogin)へ遷移するクラス
 * 履歴:
 * 2025/06/16 鶴田凌 新規作成
 */
public class Logout {
    private final Activity activity;

    /**
     * コンストラクタ
     *
     * @param activity 呼び出し元のActivity
     */
    public Logout(Activity activity) {
        this.activity = activity;
    }

    /**
     * FirebaseAuth でサインアウトし、ログイン画面へ遷移する。
     *
     * @return true=サインアウト・画面遷移成功、false=例外発生
     */
    public boolean logout() {
        try {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
