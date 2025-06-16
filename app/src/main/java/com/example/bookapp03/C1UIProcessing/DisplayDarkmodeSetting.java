package com.example.bookapp03.C1UIProcessing;

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
 */
public class DisplayDarkmodeSetting extends AppCompatActivity {

    private ImageButton btnBack;
    private Switch switchDarkmode;
    private ControlDarkmodeSwitch ctrlDark;

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

        // スイッチ制御
        ctrlDark = new ControlDarkmodeSwitch(this, switchDarkmode);
        ctrlDark.bind();
    }
}
