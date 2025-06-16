package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * モジュール名: ダークモード切替制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/16
 * 概要: ダークモードスイッチの状態を SharedPreferences に保持し、
 *       切替時に AppCompatDelegate でナイトモードを適用するクラス
 * 履歴:
 *   2025/06/16 鶴田凌 新規作成
 */
public class ControlDarkmodeSwitch {
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_DARK = "dark_mode";
    private final Activity activity;
    private final Switch sw;

    /**
     * @param activity  呼び出し元 Activity
     * @param sw        ダークモード切替スイッチ
     */
    public ControlDarkmodeSwitch(Activity activity, Switch sw) {
        this.activity = activity;
        this.sw = sw;
    }

    /**
     * スイッチの初期状態を読み込み、リスナーを設定する。
     */
    public void bind() {
        SharedPreferences sp = activity
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isDark = sp.getBoolean(KEY_DARK, false);
        // スイッチに反映
        sw.setChecked(isDark);

        // 切替時処理
        sw.setOnCheckedChangeListener((buttonView, checked) -> {
            // 1) SharedPreferences に保存
            sp.edit().putBoolean(KEY_DARK, checked).apply();
            // 2) AppCompatDelegate でモード切替
            AppCompatDelegate.setDefaultNightMode(
                checked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO
            );
            // 3) Activity 再作成でテーマを再適用
            activity.recreate();
        });
    }
}
