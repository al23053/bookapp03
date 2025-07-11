/**
 * モジュール名: 本詳細画面表示処理
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍詳細画面の表示制御を行うクラス
 * 履歴:
 * 2025/06/09 横山葉 新規作成
 */
package com.example.bookapp03.C1UIProcessing;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.bookapp03.C3BookInformationProcessing.BookDetailData;
import com.example.bookapp03.R;

/**
 * 書籍の詳細情報を画面に表示するためのコントローラー
 */
public class BookDetailViewController {

    /**
     * ログ出力用のタグ
     */
    private static final String TAG = "BookDetailViewController";

    /**
     * 書籍の詳細情報を画面に表示する。
     * データが存在しない場合は、「該当書籍なし」メッセージの表示を制御する。
     *
     * @param data              表示する書籍の詳細情報 (BookDetailDataオブジェクト)。nullの場合あり。
     * @param rootView          書籍詳細画面のアクティビティのルートビュー。findViewByIdに使用される。
     * @param showNoBookMessage データがnullのときに「該当書籍なし」メッセージを表示するかどうかを示すフラグ。
     */
    public void displayBookDetails(BookDetailData data, View rootView, boolean showNoBookMessage) {
        // データがnullの場合の処理
        if (data == null) {
            // 「該当書籍なし」メッセージを表示するかどうかで分岐
            if (showNoBookMessage) {
                TextView message = rootView.findViewById(R.id.no_book_message);
                if (message != null) {
                    message.setVisibility(View.VISIBLE); // メッセージを可視化
                }
            } else {
                // メッセージを表示しない場合はログに警告を出力
                Log.w(TAG, "bookDetailDataがnullですが、showNoBookMessageがfalseのためメッセージは表示されません。");
            }
            return; // 以降の処理は行わない
        }

        // 各UIコンポーネントの参照を取得
        TextView titleView = rootView.findViewById(R.id.book_title);
        TextView summaryView = rootView.findViewById(R.id.book_summary);
        TextView statusView = rootView.findViewById(R.id.book_status);
        ImageView coverView = rootView.findViewById(R.id.book_cover);

        // タイトルを設定
        if (titleView != null) {
            titleView.setText(data.getName());
        }

        // 概要を設定
        if (summaryView != null) {
            summaryView.setText(data.getSummary());
        }

        // 公開ステータスを設定
        if (statusView != null) {
            statusView.setText(data.getPublicStatus());
        }

        // 表紙画像をGlideライブラリを使用して非同期で読み込み、表示
        if (coverView != null) {
            Glide.with(coverView.getContext())
                    .load(data.getCoverImageUrl())
                    .into(coverView);
        }
    }
}