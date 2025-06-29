package com.example.bookapp03.C1UIProcessing;

import android.content.Intent;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

/**
 * モジュール名: ホーム画面戻りボタン制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/16
 * 概要: 戻るボタンが押されたときにホーム画面に遷移する制御クラス
 * 履歴:
 * 2025/06/16 鶴田凌 新規作成
 */
public class ControlBackToHomeButton {
    
    private static final String TAG = "ControlBackToHomeButton";
    
    private final AppCompatActivity activity;

    /**
     * コンストラクタ
     *
     * @param activity 呼び出し元Activity
     */
    public ControlBackToHomeButton(AppCompatActivity activity) {
        this.activity = activity;
    }

    /**
     * 戻るボタンにイベントリスナーを設定
     *
     * @param btnBack 戻るボタン
     */
    public void bind(ImageButton btnBack) {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> navigateToHome());
        } else {
            Log.e(TAG, "戻るボタンが null です");
        }
    }

    /**
     * ホーム画面に遷移
     */
    private void navigateToHome() {
        try {
            Log.d(TAG, "ホーム画面に戻ります");
            
            Intent intent = new Intent(activity, DisplayHome.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            
            activity.startActivity(intent);
            activity.finish();
            
        } catch (Exception e) {
            Log.e(TAG, "ホーム画面への遷移に失敗", e);
        }
    }
}