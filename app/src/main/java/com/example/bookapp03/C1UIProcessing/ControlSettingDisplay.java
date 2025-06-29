package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;

import com.example.bookapp03.R;

/**
 * モジュール名: ユーザ設定画面UI制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/16
 * 概要: 設定項目のタップを監視し、該当画面へ遷移またはログアウト処理を呼び出す
 * 履歴:
 * 2025/06/16 鶴田凌 新規作成
 */
public class ControlSettingDisplay {
    private final Activity activity;

    /**
     * コンストラクタ
     *
     * @param activity 呼び出し元の Activity
     */
    public ControlSettingDisplay(Activity activity) {
        this.activity = activity;
    }

    /**
     * 各設定項目にリスナーを設定する。
     *
     * @param btnNicknameIcon  ニックネーム・アイコン設定画面へ遷移する TextView
     * @param btnGenre         好きな本のジャンル設定画面へ遷移する62 TextView
     * @param btnDarkMode      ダークモード切替画面へ遷移する TextView
     * @param btnAccountSwitch アカウント切替画面へ遷移する TextView
     * @param btnLogout        ログアウト確認を行う TextView
     */
    public void bind(
            TextView btnNicknameIcon,
            TextView btnGenre,
            TextView btnDarkMode,
            TextView btnAccountSwitch,
            TextView btnLogout
    ) {
        btnNicknameIcon.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, AccountSettingActivity.class))
        );
        btnGenre.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, GenreSelectionActivity.class))
        );
        btnDarkMode.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, DisplayDarkmodeSetting.class))
        );
        btnAccountSwitch.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, DisplayAccountSwitching.class))
        );
        btnLogout.setOnClickListener(v ->
                new LogoutProcessing(activity).confirmLogout()
        );
    }
}
