/**
 * モジュール名: 本一覧画面表示処理
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍一覧の表示制御を行うコントローラークラス
 * 履歴:
 * 2025/06/09 横山葉 新規作成
 */

package com.example.bookup03.logic;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bookup03.data.BookListViewModel;
import com.example.bookup03.data.BookSummaryData;
import com.example.bookup03.R;
import com.example.bookup03.ui.BookListAdapter;

import java.util.List;

/**
 * 書籍一覧画面の表示処理を行う
 */
public class BookListViewController {

    /**
     * ログ出力用タグ
     */
    private static final String TAG = "BookListViewController";

    /**
     * 書籍一覧をRecyclerViewに表示する
     *
     * @param recyclerView RecyclerView本体
     * @param bookSummaries 表示する書籍サマリ一覧
     * @param viewModel ビューモデル
     * @param showEmptyMessage 書籍がないときのメッセージ表示有無
     */
    public void displayBookList(RecyclerView recyclerView, List<BookSummaryData> bookSummaries,
                                BookListViewModel viewModel, boolean showEmptyMessage) {
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerViewがnull");
            return;
        }

        // レイアウトとアダプター設定
        recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), 3));
        BookListAdapter adapter = new BookListAdapter(bookSummaries, viewModel);
        recyclerView.setAdapter(adapter);

        // 空の時のメッセージ表示処理
        View parentView = (View) recyclerView.getParent();
        TextView emptyTextView = parentView.findViewById(R.id.empty_text);

        if (emptyTextView != null) {
            emptyTextView.setVisibility(showEmptyMessage ? View.VISIBLE : View.GONE);
        }
    }
}