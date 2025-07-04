/**
 * モジュール名: 本一覧画面表示処理
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍一覧の表示制御を行うコントローラークラス。
 * 履歴:
 * 2025/06/09 横山葉 新規作成
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
 * 書籍一覧画面の表示処理を行うコントローラークラス。
 * RecyclerViewに書籍サマリを表示し、空の状態のメッセージ表示も管理します。
 */
public class BookListViewController {

    /**
     * ログ出力用のタグ
     */
    private static final String TAG = "BookListViewController";

    /**
     * 書籍一覧をRecyclerViewに表示する。
     * リストが空の場合は、空メッセージの表示状態を制御する。
     *
     * @param recyclerView     書籍一覧を表示するRecyclerView本体。
     * @param bookSummaries    RecyclerViewに表示する書籍サマリのリスト。
     * @param toggleHandler    書籍の公開/非公開切り替え操作を処理するハンドラ。
     * @param showEmptyMessage 書籍リストが空のときに「該当書籍なし」メッセージを表示するかどうか。
     */
    public void displayBookList(RecyclerView recyclerView, List<BookSummaryData> bookSummaries,
                                PublicPrivateToggleHandler toggleHandler, boolean showEmptyMessage) {
        // RecyclerViewがnullの場合はエラーログを出力し、処理を終了
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerViewがnullです。書籍リストを表示できません。");
            return;
        }

        // RecyclerViewのレイアウトマネージャーをGridLayoutManagerに設定（3列表示）
        recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), 3));
        // BookListAdapterを初期化し、RecyclerViewにアダプターを設定
        BookListAdapter adapter = new BookListAdapter(bookSummaries, toggleHandler);
        recyclerView.setAdapter(adapter);

        // 空の時のメッセージ表示処理
        // RecyclerViewの親ビューからempty_text TextViewを見つける
        View parentView = (View) recyclerView.getParent();
        TextView emptyTextView = parentView.findViewById(R.id.empty_text);

        // emptyTextViewが存在する場合、showEmptyMessageの値に基づいて表示/非表示を切り替える
        if (emptyTextView != null) {
            emptyTextView.setVisibility(showEmptyMessage ? View.VISIBLE : View.GONE);
        }
    }
}