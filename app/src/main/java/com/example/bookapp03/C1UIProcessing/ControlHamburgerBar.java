package com.example.bookapp03.C1UIProcessing;

import android.content.Intent;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp03.C5UserInformationManaging.UserAuthManager;

/**
 * モジュール名: ハンバーガーメニュー制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ハンバーガーメニューボタンの制御クラス
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 * 2025/07/01 鶴田凌 uid取得方法を修正
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
            // 最新のuidを取得（より確実な方法）
            String currentUid = UserAuthManager.getCurrentUid();
            
            Log.d(TAG, "ハイライトメモ画面を開く");
            Log.d(TAG, "使用するUID: " + currentUid);
            Log.d(TAG, "使用するVolumeID: " + volumeId);
            
            if (currentUid == null || currentUid.isEmpty()) {
                Log.e(TAG, "UIDが取得できません");
                Toast.makeText(activity, "ユーザー情報を取得できませんでした", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (volumeId == null || volumeId.isEmpty()) {
                Log.e(TAG, "VolumeIDが設定されていません");
                Toast.makeText(activity, "書籍を選択してください", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Intent intent = new Intent(activity, DisplayHighlightMemo.class);
            intent.putExtra("uid", currentUid);
            intent.putExtra("volumeId", volumeId);
            
            Log.d(TAG, "Intent作成完了 - UID: " + currentUid + ", VolumeID: " + volumeId);
            
            // 新しい画面として起動
            activity.startActivity(intent);
            
        } catch (Exception e) {
            Log.e(TAG, "ハイライトメモ画面の起動に失敗", e);
            Toast.makeText(activity, "画面の起動に失敗しました: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
