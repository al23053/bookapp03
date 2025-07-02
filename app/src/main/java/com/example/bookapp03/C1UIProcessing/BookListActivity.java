/**
 * モジュール名: BookListActivity
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍一覧を表示するActivity。初期データを生成し、RecyclerViewに表示する。
 * 履歴:
 * 2025/06/09 横山葉 新規作成
 */

package com.example.bookapp03.C1UIProcessing;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp03.R;
import com.example.bookapp03.C6BookInformationManaging.BookRepositoryImpl; // ViewModelFactoryで処理するので不要
import com.example.bookapp03.C3BookInformationProcessing.BookListViewModel; // ViewModelのパッケージ
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.bookapp03.C5UserInformationManaging.UserAuthManager; // UserAuthManagerをインポート

public class BookListActivity extends AppCompatActivity {

    private BookListViewModel viewModel;
    private BookListViewController controller;
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private PublicPrivateToggleHandler toggleHandler; // ★追加★

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        controller = new BookListViewController();
        recyclerView = findViewById(R.id.book_recycler_view);
        emptyTextView = findViewById(R.id.empty_text);

        // ViewModelProviderを使用してViewModelを初期化
        viewModel = new ViewModelProvider(this, new ViewModelFactory(getApplicationContext()))
                .get(BookListViewModel.class);

        // PublicPrivateToggleHandlerを初期化 (ViewModelを渡す)
        toggleHandler = new PublicPrivateToggleHandler(viewModel); // ★追加★

        // LiveDataの変更を監視
        // ViewModelのbookListが更新されたら、自動的にここが呼ばれる
        viewModel.bookList.observe(this, bookSummaries -> {
            // データが更新されたらUIを更新
            boolean isEmpty = bookSummaries == null || bookSummaries.isEmpty();

            // controller.displayBookListの引数を変更: ViewModelの代わりにtoggleHandlerを渡す
            controller.displayBookList(recyclerView, bookSummaries, toggleHandler, isEmpty);

            // 空の時のメッセージ表示制御
            if (isEmpty) {
                emptyTextView.setVisibility(View.VISIBLE);
            } else {
                emptyTextView.setVisibility(View.GONE);
            }
        });

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setOnItemSelectedListener(item -> {
            new ControlBottomNavigationBar().handledisplay(item.getItemId(), this);
            return true;
        });

        // Activityが作成されたらViewModelにデータのロードを指示
        // ユーザーUIDを取得するロジックをここに実装
        String currentUserId = UserAuthManager.getCurrentUid(); // UserAuthManagerからUIDを取得
        if (currentUserId != null) {
            viewModel.loadBooks(currentUserId);
        } else {
            // UIDが取得できない場合のハンドリング（例: ログイン画面への遷移、エラーメッセージ表示など）
            // 現時点ではログ出力のみ
            android.util.Log.e("BookListActivity", "User not logged in or UID not available.");
            // 必要に応じて、空のリストを表示するか、ログインを促すUIを表示
            controller.displayBookList(recyclerView, new java.util.ArrayList<>(), toggleHandler, true);
        }
    }
}