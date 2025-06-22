/**
 * モジュール名: BookListActivity
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍一覧を表示するActivity。初期データを生成し、RecyclerViewに表示する。
 * 履歴:
 *   2025/06/09 横山葉 新規作成
 */

package com.example.bookapp03.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp03.R;
import com.example.bookapp03.data.repository.BookRepositoryImpl; // 新しいimport
import com.example.bookapp03.logic.BookListViewController;
import com.example.bookapp03.presentation.viewmodel.BookListViewModel; // ViewModelのパッケージ変更

import java.util.List;

public class BookListActivity extends AppCompatActivity {

    private BookListViewModel viewModel;
    private BookListViewController controller;
    private RecyclerView recyclerView;
    private TextView emptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        controller = new BookListViewController();
        recyclerView = findViewById(R.id.book_list_recycler);
        emptyTextView = findViewById(R.id.empty_text); // レイアウトにempty_textがあることを前提

        // ViewModelProviderを使用してViewModelを初期化
        // ViewModelのコンストラクタにRepositoryを渡すためのFactoryが必要
        // ViewModelFactoryは別途定義します
        viewModel = new ViewModelProvider(this, new ViewModelFactory(getApplicationContext()))
                .get(BookListViewModel.class);

        // LiveDataの変更を監視
        // ViewModelのbookListが更新されたら、自動的にここが呼ばれる
        viewModel.bookList.observe(this, bookSummaries -> {
            // データが更新されたらUIを更新
            boolean isEmpty = bookSummaries == null || bookSummaries.isEmpty();
            controller.displayBookList(recyclerView, bookSummaries, viewModel, isEmpty);

            // 空の時のメッセージ表示制御
            if (isEmpty) {
                emptyTextView.setVisibility(View.VISIBLE);
            } else {
                emptyTextView.setVisibility(View.GONE);
            }
        });

        // Activityが作成されたらViewModelにデータのロードを指示
        // TODO: 実際のユーザーUIDを取得するロジックをここに実装
        String currentUserId = "user123"; // 仮のユーザーID
        viewModel.loadBooks(currentUserId);
    }
}