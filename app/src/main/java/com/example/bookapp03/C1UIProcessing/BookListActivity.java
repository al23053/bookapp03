/**
 * モジュール名: 本一覧表示
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: ユーザーの読書記録を一覧表示し、新規書籍の追加や公開・非公開状態の切り替えを可能にするアクティビティ。
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
import com.example.bookapp03.C3BookInformationProcessing.BookListViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.bookapp03.C5UserInformationManaging.UserAuthManager;

/**
 * 書籍一覧画面を表示するアクティビティ。
 * ユーザーの書籍リストを管理し、UIに表示します。
 */
public class BookListActivity extends AppCompatActivity {

    /** 書籍リストデータを管理するViewModel */
    private BookListViewModel viewModel;
    /** 書籍リストのUI表示を制御するコントローラー */
    private BookListViewController controller;
    /** 書籍リストを表示するためのRecyclerView */
    private RecyclerView recyclerView;
    /** 書籍リストが空の場合に表示するTextView */
    private TextView emptyTextView;
    /** 書籍の公開・非公開状態の切り替え操作を処理するハンドラー */
    private PublicPrivateToggleHandler toggleHandler;

    /**
     * アクティビティが最初に作成されたときに呼び出される。
     * UIの初期設定、ViewModelの初期化、データ監視、イベントハンドラの設定を行う。
     *
     * @param savedInstanceState アクティビティの以前の保存状態を含むBundleオブジェクト。
     * アクティビティが以前に存在し、最後に終了していなかった場合に非nullとなる。
     */
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
        toggleHandler = new PublicPrivateToggleHandler(viewModel);

        // LiveDataの変更を監視
        // ViewModelのbookListが更新されたら、自動的にこのobserverが呼ばれる
        viewModel.bookList.observe(this, bookSummaries -> {
            // データが更新されたらUIを更新
            boolean isEmpty = bookSummaries == null || bookSummaries.isEmpty();

            // controller.displayBookListの引数を変更: ViewModelの代わりにtoggleHandlerを渡す
            controller.displayBookList(recyclerView, bookSummaries, toggleHandler, isEmpty);

            // 空の時のメッセージ表示制御
            if (isEmpty) {
                emptyTextView.setVisibility(View.VISIBLE); // リストが空の場合はメッセージを表示
            } else {
                emptyTextView.setVisibility(View.GONE);    // リストにデータがある場合はメッセージを非表示
            }
        });

        // ボトムナビゲーションバーのアイテム選択リスナーを設定
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setOnItemSelectedListener(item -> {
            new ControlBottomNavigationBar().handledisplay(item.getItemId(), this);
            return true;
        });

        // Activityが作成されたらViewModelにデータのロードを指示
        // ユーザーUIDを取得するロジックを実装
        String currentUserId = UserAuthManager.getCurrentUid(); // UserAuthManagerから現在のユーザーUIDを取得
        if (currentUserId != null) {
            viewModel.loadBooks(currentUserId); // ViewModelに書籍リストのロードを指示
        } else {
            // UIDが取得できない場合のハンドリング（例: ログイン画面への遷移、エラーメッセージ表示など）
            android.util.Log.e("BookListActivity", "User not logged in or UID not available.");
            // 必要に応じて、空のリストを表示するか、ログインを促すUIを表示
            controller.displayBookList(recyclerView, new java.util.ArrayList<>(), toggleHandler, true);
        }
    }
}