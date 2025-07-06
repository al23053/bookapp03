package com.example.bookapp03.C1UIProcessing;

import android.content.Context;
import android.content.Intent;

import com.example.bookapp03.R;
import com.example.bookapp03.Searchmain.DisplaySearchBookNameWindow;
import com.example.bookapp03.ui.BookSelectionActivity;


/**
 * モジュール名: ボトムナビゲーションバー制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ナビゲーションバーのアイテム選択に応じて画面遷移を制御するクラス
 * 履歴:
 * 　2025/06/15 鶴田凌 新規作成
 */
public class ControlBottomNavigationBar {

    /**
     * 選択されたメニュー ID によって対応する画面へ遷移を行う。
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
        // 他のメニューは必要に応じて追加
        if (intent != null) {
            context.startActivity(intent);
        }
    }
}

