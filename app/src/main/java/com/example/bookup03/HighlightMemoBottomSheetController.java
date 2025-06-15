package com.example.bookup03;

import android.view.View;
import android.widget.TextView;

public class HighlightMemoBottomSheetController {

    public void displayMemo(View rootView, String memoText) {
        if (rootView == null) return;

        TextView memoTextView = rootView.findViewById(R.id.highlight_memo_text);
        if (memoTextView != null) {
            memoTextView.setText(memoText != null ? memoText : "");
        }
    }
}