/**
 * モジュール名: 本一覧画面表示処理
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍一覧の表示制御を行うコントローラークラス
 * 履歴:
 * 2025/06/09 横山葉 新規作成
 */

package com.example.bookapp03.C1UIProcessing;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// import com.example.bookapp03.C3BookInformationProcessing.BookListViewModel; // ViewModelは直接不要になるためコメントアウト/削除
import com.example.bookapp03.C3BookInformationProcessing.BookSummaryData;
import com.example.bookapp03.R;

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
     * @param toggleHandler 公開/非公開切り替えハンドラ ★型を変更★
     * @param showEmptyMessage 書籍がないときのメッセージ表示有無
     */
    public void displayBookList(RecyclerView recyclerView, List<BookSummaryData> bookSummaries,
                                PublicPrivateToggleHandler toggleHandler, boolean showEmptyMessage) { // ★シグネチャ変更★
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerViewがnull");
            return;
        }

        // レイアウトとアダプター設定
        recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), 3));
        BookListAdapter adapter = new BookListAdapter(bookSummaries, toggleHandler); // ★修正: viewModelをtoggleHandlerに変更★
        recyclerView.setAdapter(adapter);

        // 空の時のメッセージ表示処理
        View parentView = (View) recyclerView.getParent();
        TextView emptyTextView = parentView.findViewById(R.id.empty_text);

        if (emptyTextView != null) {
            emptyTextView.setVisibility(showEmptyMessage ? View.VISIBLE : View.GONE);
        }
    }
}