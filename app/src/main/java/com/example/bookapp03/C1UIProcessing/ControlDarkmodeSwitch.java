package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import android.widget.Switch;

/**
 * モジュール名: ダークモード切替制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/16
 * 概要: ダークモードスイッチの状態を永続化し、再起動時にも設定を復元するクラス
 * 履歴:
 *   2025/06/16 鶴田凌 新規作成
 *   2025/07/06 鶴田凌 再起動時の状態維持対応
 */
public class ControlDarkmodeSwitch {
    /** SharedPreferences ファイル名 */
    private static final String PREFS_NAME = "app_prefs";
    /** ダークモード設定キー */
    private static final String KEY_DARK   = "dark_mode";

    private final Activity      activity;
    private final SharedPreferences prefs;

    /**
     * @param activity ダークモード切替を行う Activity
     */
    public ControlDarkmodeSwitch(Activity activity) {
        this.activity = activity;
        this.prefs    = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Switch に初期状態をセットし、トグル時に保存＆テーマ適用を行う
     *
     * @param switchDarkmode ダークモード切替用 Switch
     */
    public void bind(Switch switchDarkmode) {
        // 1) 保存済み設定を読み出して初期状態に
        boolean isDark = prefs.getBoolean(KEY_DARK, false);
        switchDarkmode.setChecked(isDark);

        // 2) スイッチ切り替え時の処理
        switchDarkmode.setOnCheckedChangeListener((btn, checked) -> {
            // 保存
            prefs.edit().putBoolean(KEY_DARK, checked).apply();
            // テーマ適用
            AppCompatDelegate.setDefaultNightMode(
                checked
                  ? AppCompatDelegate.MODE_NIGHT_YES
                  : AppCompatDelegate.MODE_NIGHT_NO
            );
            // Activity 再生成で即時反映
            activity.recreate();
        });
    }
}
