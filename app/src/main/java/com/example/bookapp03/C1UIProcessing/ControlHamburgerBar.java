package com.example.bookapp03.C1UIProcessing;

import android.util.Log;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

/**
 * モジュール名: ハンバーガーメニュー制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ハンバーガーメニューボタンの制御クラス（別画面で表示）
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 * 2025/07/01 鶴田凌 既存のDisplayHighlightMemoを使用
 */
public class ControlHamburgerBar {
    private static final String TAG = "ControlHamburgerBar";
    private final DrawerLayout drawer;
    private final String uid;
    private final String volumeId;

    /**
     * コンストラクタ
     *
     * @param act         呼び出し元Activity
     * @param drawerLayout ドロワーレイアウト
     * @param uid         ユーザーID
     * @param volumeId    ボリュームID
     */
    public ControlHamburgerBar(AppCompatActivity act,
                               DrawerLayout drawerLayout,
                               String uid,
                               String volumeId) {
        this.drawer = drawerLayout;
        this.uid = uid;
        this.volumeId = volumeId;
        Log.d(TAG, "init - UID: " + uid + ", VolumeID: " + volumeId);
    }

    /**
     * ハンバーガーメニューボタンにイベントリスナーを設定
     *
     * @param btnMenu ハンバーガーメニューボタン
     */
    public void bind(ImageButton btnMenu) {
        btnMenu.setOnClickListener(v -> toggle());
    }

    /**
     * ドロワーの開閉をトグル
     */
    private void toggle() {
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
            Log.d(TAG, "drawer closed");
        } else {
            // 必要ならここでデータロード (uid, volumeId) を呼ぶ
            drawer.openDrawer(GravityCompat.END);
            Log.d(TAG, "drawer opened");
        }
    }
}
