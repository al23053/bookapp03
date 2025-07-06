package com.example.bookapp03.C1UIProcessing;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

/**
 * モジュール名: 全体まとめ登録画面への遷移ボタン制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: 全体まとめ登録画面へ遷移するボタンを制御するクラス
 * 履歴:
 * 　2025/06/15 鶴田凌 新規作成
 */
public class ControlButtonToSummary {
    
    /**
     * 全体まとめ画面に遷移（volumeIdと本の名前を渡す）
     */
    public void setToSummary(AppCompatActivity activity, String volumeId, String bookTitle) {
        Intent intent = new Intent(activity, DisplaySummary.class);
        intent.putExtra("volumeId", volumeId);
        intent.putExtra("bookTitle", bookTitle);
        activity.startActivity(intent);
    }
    
    /**
     * 全体まとめ画面に遷移（volumeIdのみ）
     */
    public void setToSummary(AppCompatActivity activity, String volumeId) {
        Intent intent = new Intent(activity, DisplaySummary.class);
        intent.putExtra("volumeId", volumeId);
        activity.startActivity(intent);
    }
}
