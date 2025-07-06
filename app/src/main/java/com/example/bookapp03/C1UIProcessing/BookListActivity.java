/**
 * モジュール名: 本一覧表示
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: ユーザーの読書記録を一覧表示し、新規書籍の追加や公開・非公開状態の切り替えを可能にするアクティビティ。
 * 履歴:
 *   2025/06/09 横山葉 新規作成
 */

package com.example.bookapp03.C1UIProcessing;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp03.R;
import com.example.bookapp03.C3BookInformationProcessing.BookListViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.bookapp03.C5UserInformationManaging.UserAuthManager;

import java.util.ArrayList;

/**
 * 書籍一覧画面を表示するアクティビティ。
 * ユーザーの書籍リストを管理し、UIに表示します。
 */
public class BookListActivity extends AppCompatActivity {

    /**
     * 書籍リストの状態を保持・更新するViewModel
     */
    private BookListViewModel viewModel;

    /**
     * 書籍一覧のUI表示制御を行うコントローラー
     */
    private BookListViewController controller;

    /**
     * 書籍一覧を表示するRecyclerView
     */
    private RecyclerView recyclerView;

    /**
     * 書籍リストが空のときに表示するテキストビュー
     */
    private TextView emptyTextView;

    /**
     * 書籍の公開・非公開状態切り替えハンドラー
     */
    private PublicPrivateToggleHandler toggleHandler;

    /**
     * アクティビティ作成時の初期化処理
     * @param savedInstanceState 保存されたインスタンス状態
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        controller = new BookListViewController();
        recyclerView = findViewById(R.id.book_recycler_view);
        emptyTextView = findViewById(R.id.empty_text);

        viewModel = new ViewModelProvider(this, new ViewModelFactory(getApplicationContext()))
                .get(BookListViewModel.class);

        toggleHandler = new PublicPrivateToggleHandler(viewModel);

        // ユーザーIDを取得
        String currentUserId = UserAuthManager.getCurrentUid();

        // LiveDataを監視
        viewModel.bookList.observe(this, bookSummaries -> {
            boolean isEmpty = bookSummaries == null || bookSummaries.isEmpty();
            controller.displayBookList(
                    recyclerView,
                    isEmpty ? new ArrayList<>() : bookSummaries,
                    currentUserId != null ? currentUserId : "",
                    toggleHandler,
                    isEmpty
            );
            emptyTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });

        // ボトムナビゲーションのリスナー設定
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setOnItemSelectedListener(item -> {
            new ControlBottomNavigationBar().handledisplay(item.getItemId(), this);
            return true;
        });

        if (currentUserId != null) {
            viewModel.loadBooks(currentUserId);
        } else {
            android.util.Log.e("BookListActivity", "User not logged in or UID not available.");
            controller.displayBookList(recyclerView, new ArrayList<>(), "", toggleHandler, true);
        }
    }
}
