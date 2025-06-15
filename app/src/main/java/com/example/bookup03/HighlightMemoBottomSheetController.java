/**
 * モジュール名: ボトムシート表示処理
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: ハイライトメモの表示を管理するコントローラクラス
 * 履歴:
 * 2025/06/09 横山葉 新規作成
 */

package com.example.bookup03;

import android.view.View;
import android.widget.TextView;

/**
 * ハイライトメモの表示処理を担当するクラス
 */
public class HighlightMemoBottomSheetController {

    /**
     * メモを表示する
     * @param rootView メモを表示する親ビュー
     * @param memoText 表示するメモのテキスト
     */
    public void displayMemo(View rootView, String memoText) {
        if (rootView == null) return;

        TextView memoTextView = rootView.findViewById(R.id.highlight_memo_text);
        if (memoTextView != null) {
            memoTextView.setText(memoText != null ? memoText : "");
        }
    }
}
