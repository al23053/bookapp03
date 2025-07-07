package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.bookapp03.R;
import com.example.bookapp03.Searchmain.DisplaySearchBookNameWindow;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * モジュール名: ボトムナビゲーションバー制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ナビゲーションバーのアイテム選択に応じて画面遷移を制御し、
 *       現在表示中の画面に対応するアイテムを選択状態にするクラス
 * 履歴:
 *   2025/06/15 鶴田凌 新規作成
 *   2025/07/07 鶴田凌 現在画面との連動機能追加
 */
public class ControlBottomNavigationBar {

    /**
     * 選択されたメニュー ID によって対応する画面へ遷移を行う
     *
     * @param itemId  選択されたメニューアイテムのリソース ID
     * @param context 遷移先 Activity を起動するコンテキスト
     */
    public void handledisplay(int itemId, Context context) {
        Intent intent = null;
        if (itemId == R.id.nav_home) {
            intent = new Intent(context, DisplayHome.class);
        } else if (itemId == R.id.nav_search) {
            intent = new Intent(context, DisplaySearchBookNameWindow.class);
        } else if (itemId == R.id.nav_book) {
            intent = new Intent(context, BookListActivity.class);
        }
        
        if (intent != null) {
            context.startActivity(intent);
            // 遷移後は現在の Activity を終了
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        }
    }

    /**
     * 現在表示中の Activity に対応するナビゲーションアイテムを選択状態にする
     *
     * @param activity 現在表示中の Activity
     * @param bottomNav BottomNavigationView
     */
    public void setCurrentItem(Activity activity, BottomNavigationView bottomNav) {
        String className = activity.getClass().getSimpleName();
        int selectedItemId = R.id.nav_home; // デフォルトはホーム
        
        switch (className) {
            case "DisplayHome":
                selectedItemId = R.id.nav_home;
                break;
            case "DisplaySearchBookNameWindow":
                selectedItemId = R.id.nav_search;
                break;
            case "BookListActivity":
                selectedItemId = R.id.nav_book;
                break;
        }
        
        // final変数として宣言
        final int currentSelectedId = selectedItemId;
        
        // 選択状態を設定（リスナーを一時的に無効化して無限ループを防ぐ）
        bottomNav.setOnItemSelectedListener(null);
        bottomNav.setSelectedItemId(currentSelectedId);
        
        // リスナーを再設定
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() != currentSelectedId) {
                handledisplay(item.getItemId(), activity);
            }
            return true;
        });
    }
}

