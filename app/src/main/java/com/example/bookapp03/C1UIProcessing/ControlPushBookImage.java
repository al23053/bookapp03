/**
 * モジュール名: ControlPushBookImage
 * 作成者: 三浦寛生
 * 作成日: 2025/06/15
 * 概要: 本の画像の押下を処理するActivityです。
 * 履歴:
 * 2025/06/15 三浦寛生 新規作成
 */
package com.example.bookapp03.C1UIProcessing;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.bookapp03.model.Book;

public class ControlPushBookImage {
    private static final String TAG = "ControlPushBookImage";
    private final Context context;

    public ControlPushBookImage(Context context){
        this.context = context;
    }

    /**
     * 書籍がクリックされた際の処理。
     * BookSelectionActivityへ遷移し、書籍IDとタイトルを渡します。
     * @param book クリックされた書籍オブジェクト
     */
    public void handleBookClick(Book book) {
        if (book == null) {
            Log.e(TAG, "クリックされたBookオブジェクトがnullです。");
            return;
        }

        Log.d(TAG, "「" + book.getTitle() + "」がクリックされました。BookSelectionActivityへ遷移します。");
        Intent intent = new Intent(context, BookSelectionActivity.class);
        intent.putExtra("bookId", book.getId());
        intent.putExtra("bookTitle", book.getTitle());
        // ActivityのコンテキストからActivityを開始する場合、FLAG_ACTIVITY_NEW_TASKは通常不要ですが、
        // Application Contextなどから開始する場合は必要になることがあります。
        // ここではSearchResultActivityから呼ばれることを想定しているので不要です。
        context.startActivity(intent);
    }
}
