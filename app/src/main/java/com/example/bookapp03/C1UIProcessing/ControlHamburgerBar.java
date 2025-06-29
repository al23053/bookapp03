package com.example.bookapp03.C1UIProcessing;

import android.content.Intent;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

/**
 * モジュール名: ハンバーガーメニュー制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ハンバーガーメニューボタンの制御クラス
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
public class ControlHamburgerBar {
    
    private static final String TAG = "ControlHamburgerBar";
    
    private final AppCompatActivity activity;
    private final String uid;
    private final String volumeId;

    /**
     * コンストラクタ
     *
     * @param activity 呼び出し元Activity
     * @param uid      ユーザID
     * @param volumeId 書籍ID
     */
    public ControlHamburgerBar(AppCompatActivity activity, String uid, String volumeId) {
        this.activity = activity;
        this.uid = uid;
        this.volumeId = volumeId;
    }

    /**
     * ハンバーガーメニューボタンにイベントリスナーを設定
     *
     * @param btnMenu ハンバーガーメニューボタン
     */
    public void bind(ImageButton btnMenu) {
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> openHighlightMemoScreen());
        } else {
            Log.e(TAG, "ハンバーガーメニューボタンが null です");
        }
    }

    /**
     * ハイライトメモ画面を開く
     */
    private void openHighlightMemoScreen() {
        try {
            Log.d(TAG, "ハイライトメモ画面を開く");
            
            Intent intent = new Intent(activity, DisplayHighlightMemo.class);
            intent.putExtra("uid", uid);
            intent.putExtra("volumeId", volumeId);
            
            // 新しい画面として起動（戻るボタンで元画面に戻る）
            activity.startActivity(intent);
            
        } catch (Exception e) {
            Log.e(TAG, "ハイライトメモ画面の起動に失敗", e);
        }
    }
}
