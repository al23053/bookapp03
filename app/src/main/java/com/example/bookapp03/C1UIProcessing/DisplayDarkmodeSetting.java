package com.example.bookapp03.C1UIProcessing;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp03.R;

/**
 * モジュール名: ダークモード切替画面表示
 * 作成者: 鶴田凌
 * 作成日: 2025/06/16
 * 概要: ダークモード切替用 UI を表示し、スイッチを ControlDarkmodeSwitch で制御する Activity
 * 履歴:
 * 2025/06/16 鶴田凌 新規作成
 * 2025/07/07 鶴田凌 スイッチ状態復元機能追加
 */
public class DisplayDarkmodeSetting extends AppCompatActivity {
    /** SharedPreferences ファイル名 */
    private static final String PREFS_NAME = "app_prefs";
    /** ダークモード設定キー */
    private static final String KEY_DARK   = "dark_mode";

    private ImageButton btnBack;
    private ControlDarkmodeSwitch ctrlDark;
    private Switch switchDarkmode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.darkmodesettingdisplay);

        // View バインド
        btnBack = findViewById(R.id.btnBack);
        switchDarkmode = findViewById(R.id.switchDarkmode);

        // 戻るボタン
        btnBack.setOnClickListener(v ->
                new ControlButtonToSetting().setToSetting(this)
        );

        // 保存済み設定を読み込んでスイッチの初期状態を設定
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_DARK, false);
        switchDarkmode.setChecked(isDark);

        // スイッチ制御
        ctrlDark = new ControlDarkmodeSwitch(this);
        ctrlDark.bind(switchDarkmode);
    }
}
