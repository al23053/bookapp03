package com.example.bookapp03.C1UIProcessing;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp03.R;

/**
 * モジュール名: ユーザ設定画面表示
 * 作成者: 鶴田凌
 * 作成日: 2025/06/16
 * 概要: settingdisplay.xml をセットし、UI コントローラを初期化する Activity
 * 履歴:
 *   2025/06/16 鶴田凌 新規作成
 */
public class DisplaySetting extends AppCompatActivity {

    private static final String TAG = "DisplaySetting";

    private ImageButton btnBack;
    private TextView btnNicknameIcon;
    private TextView btnGenre;
    private TextView btnDarkMode;
    private TextView btnAccountSwitch;
    private TextView btnLogout;

    private ControlBackToHomeButton ctrlBack;
    private ControlSettingDisplay ctrlSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingdisplay);

        Log.d(TAG, "DisplaySetting onCreate開始");

        try {
            // View バインド
            initializeViews();

            // コントローラー設定
            setupControllers();

        } catch (Exception e) {
            Log.e(TAG, "DisplaySetting 初期化エラー", e);
        }
    }

    /**
     * View要素を初期化
     */
    private void initializeViews() {
        btnBack           = findViewById(R.id.btnBack);
        btnNicknameIcon   = findViewById(R.id.btnNicknameIcon);
        btnGenre          = findViewById(R.id.btnGenre);
        btnDarkMode       = findViewById(R.id.btnDarkMode);
        btnAccountSwitch  = findViewById(R.id.btnAccountSwitch);
        btnLogout         = findViewById(R.id.btnLogout);
    }

    /**
     * コントローラーを設定
     */
    private void setupControllers() {
        // 戻るボタン制御
        ctrlBack = new ControlBackToHomeButton(this);
        ctrlBack.bind(btnBack);

        // 設定項目制御
        ctrlSettings = new ControlSettingDisplay(this);
        ctrlSettings.bind(
            btnNicknameIcon,
            btnGenre,
            btnDarkMode,
            btnAccountSwitch,
            btnLogout
        );
    }
}
