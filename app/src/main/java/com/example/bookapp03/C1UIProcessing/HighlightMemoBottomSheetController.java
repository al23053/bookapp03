/**
 * モジュール名: ボトムシート表示処理
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: ハイライトメモの表示を管理するコントローラクラス
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
 * ハイライトメモの表示処理を担当するクラス
 */
public class HighlightMemoBottomSheetController {

    /**
     * メモを表示する
     * @param rootView メモを表示する親ビュー
     * @param memos 表示するメモのテキスト
     */
    public void displayMemo(View rootView, List<HighlightMemoData> memos) {
        if (rootView == null) return;

        TableLayout table = rootView.findViewById(R.id.highlight_memo_table);
        if (table == null) return;

        table.removeViews(1, table.getChildCount() - 1); // ヘッダー以外を一旦全部削除

        if (memos == null || memos.isEmpty()) {
            // メモなしのときは1行表示
            TableRow emptyRow = new TableRow(rootView.getContext());
            TextView emptyText = new TextView(rootView.getContext());
            emptyText.setText("ハイライトメモはありません。");
            emptyText.setPadding(8,8,8,8);
            emptyRow.addView(emptyText);
            table.addView(emptyRow);
            return;
        }

        for (HighlightMemoData memo : memos) {
            TableRow row = new TableRow(rootView.getContext());

            TextView pageText = new TextView(rootView.getContext());
            pageText.setText(String.valueOf(memo.getPage()));
            pageText.setPadding(8,4,8,4);
            row.addView(pageText);

            TextView lineText = new TextView(rootView.getContext());
            lineText.setText(String.valueOf(memo.getLine()));
            lineText.setPadding(8,4,8,4);
            row.addView(lineText);

            TextView memoText = new TextView(rootView.getContext());
            memoText.setText(memo.getMemo());
            memoText.setPadding(8,4,8,4);
            row.addView(memoText);

            table.addView(row);
        }
    }
}
