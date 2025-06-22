/**
 * モジュール名: BookListActivity
 * 作成者: ユーザー名
 * 作成日: 2025/06/09
 * 概要: 書籍一覧を表示するActivity。初期データを生成し、RecyclerViewに表示する。
 * 履歴:
 *   2025/06/09 ユーザー名 新規作成
 */

package com.example.bookapp03.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp03.R;
import com.example.bookapp03.logic.BookListViewController;
import com.example.bookapp03.data.BookListViewModel;
import com.example.bookapp03.data.BookSummaryData;

import java.util.ArrayList;
import java.util.List;

public class BookListActivity extends AppCompatActivity {

    /**
     * 書籍一覧の表示・管理用ViewModel
     */
    private BookListViewModel viewModel;

    /**
     * Activity起動時に呼び出される。初期化処理とRecyclerViewの設定を行う。
     * @param savedInstanceState 保存されたインスタンス状態（未使用）
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new BookListViewModel();

        // 初期データを仮で用意
        List<BookSummaryData> initialBookList = new ArrayList<>();
        initialBookList.add(new BookSummaryData("1", "本A", "https://example.com/sample1.jpg"));
        initialBookList.add(new BookSummaryData("2", "本B", "https://example.com/sample2.jpg"));
        initialBookList.add(new BookSummaryData("3", "本C", "https://example.com/sample3.jpg"));

        // 全て非公開に設定
        for (BookSummaryData book : initialBookList) {
            book.setPublic(false);
        }

        viewModel.setBooks(initialBookList);

        // RecyclerViewに書籍リストを表示
        RecyclerView recyclerView = findViewById(R.id.book_list_recycler);
        boolean isEmpty = initialBookList.isEmpty();

        BookListViewController controller = new BookListViewController();
        controller.displayBookList(recyclerView, initialBookList, viewModel, isEmpty);

        // TODO: データベースから実データを取得するように変更する (ユーザー名)
    }
}