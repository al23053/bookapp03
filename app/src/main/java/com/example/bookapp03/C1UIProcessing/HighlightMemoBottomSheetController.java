/**
 * モジュール名: ボトムシート表示処理
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: ハイライトメモの表示を管理するコントローラクラス。
 * 履歴:
 * 2025/06/09 横山葉 新規作成
 */
package com.example.bookapp03.C1UIProcessing;

import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.List;

import com.example.bookapp03.R;

/**
 * ハイライトメモの表示処理を担当するクラス。
 * ボトムシート内のテーブルにハイライトメモのリストを表示します。
 */
public class HighlightMemoBottomSheetController {

    /**
     * 指定された親ビュー内のテーブルにハイライトメモのリストを表示する。
     * 既存のメモをクリアし、新しいメモを動的に追加します。
     * メモが空の場合は、「ハイライトメモはありません」というメッセージを表示します。
     *
     * @param rootView メモを表示するボトムシートの親ビュー。R.id.highlight_memo_tableを含む必要があります。
     * @param memos    表示するHighlightMemoDataオブジェクトのリスト。
     */
    public void displayMemo(View rootView, List<HighlightMemoData> memos) {
        // 親ビューがnullの場合は処理を中断
        if (rootView == null) return;

        // テーブルレイアウトの参照を取得
        TableLayout table = rootView.findViewById(R.id.highlight_memo_table);
        // テーブルがnullの場合は処理を中断
        if (table == null) return;

        // ヘッダー行（インデックス0）以外のすべての既存ビューを削除
        // これにより、新しいメモを表示する前に古い表示がクリアされる
        table.removeViews(1, table.getChildCount() - 1);

        // メモリストがnullまたは空の場合の処理
        if (memos == null || memos.isEmpty()) {
            // 「ハイライトメモはありません」というメッセージを表示するための一行を作成
            TableRow emptyRow = new TableRow(rootView.getContext());
            TextView emptyText = new TextView(rootView.getContext());
            emptyText.setText("ハイライトメモはありません。");
            emptyText.setPadding(8, 8, 8, 8); // パディングを設定
            emptyRow.addView(emptyText); // 行にテキストビューを追加
            table.addView(emptyRow); // テーブルに行を追加
            return; // メモがないため、これ以降の処理は不要
        }

        // メモリストをループして各メモの情報をテーブルに追加
        for (HighlightMemoData memo : memos) {
            TableRow row = new TableRow(rootView.getContext()); // 新しい行を作成

            // ページ番号のTextViewを作成し、行に追加
            TextView pageText = new TextView(rootView.getContext());
            pageText.setText(String.valueOf(memo.getPage()));
            pageText.setPadding(8, 4, 8, 4); // パディングを設定
            row.addView(pageText);

            // 行番号のTextViewを作成し、行に追加
            TextView lineText = new TextView(rootView.getContext());
            lineText.setText(String.valueOf(memo.getLine()));
            lineText.setPadding(8, 4, 8, 4); // パディングを設定
            row.addView(lineText);

            // メモ本文のTextViewを作成し、行に追加
            TextView memoText = new TextView(rootView.getContext());
            memoText.setText(memo.getMemo());
            memoText.setPadding(8, 4, 8, 4); // パディングを設定
            row.addView(memoText);

            table.addView(row); // 作成した行をテーブルに追加
        }
    }
}