/**
 * モジュール名: 本一覧画面表示処理
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍一覧の表示制御を行うコントローラークラス。
 * 履歴:
 *   2025/06/09 横山葉 新規作成
 */

package com.example.bookapp03.C1UIProcessing;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookapp03.C3BookInformationProcessing.BookSummaryData;
import com.example.bookapp03.R;

import java.util.List;

/**
 * 書籍一覧のUI表示制御を担当するクラス
 */
public class BookListViewController {

    /**
     * ログ出力用のタグ
     */
    private static final String TAG = "BookListViewController";

    /**
     * 書籍一覧をRecyclerViewに表示する
     *
     * @param recyclerView     RecyclerView本体
     * @param bookSummaries    表示対象の書籍サマリ一覧
     * @param uid              ユーザーID（公開状態更新に使用）
     * @param toggleHandler    公開・非公開切り替え操作のハンドラ
     * @param showEmptyMessage 書籍が存在しないときのメッセージ表示フラグ
     */
    public void displayBookList(RecyclerView recyclerView, List<BookSummaryData> bookSummaries,
                                String uid, PublicPrivateToggleHandler toggleHandler, boolean showEmptyMessage) {
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerViewがnullです。書籍リストを表示できません。");
            return;
        }

        recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), 3));
        BookListAdapter adapter = new BookListAdapter(bookSummaries, uid, toggleHandler);
        recyclerView.setAdapter(adapter);

        View parentView = (View) recyclerView.getParent();
        TextView emptyTextView = parentView.findViewById(R.id.empty_text);
        if (emptyTextView != null) {
            emptyTextView.setVisibility(showEmptyMessage ? View.VISIBLE : View.GONE);
        }
    }
}
