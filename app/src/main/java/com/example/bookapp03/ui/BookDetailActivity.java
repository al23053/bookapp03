/**
 * モジュール名: BookDetailActivity
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍詳細画面を表示するActivity。インテントで受け取った情報を元に書籍情報を表示する。
 * 履歴:
 *   2025/06/09 横山葉 新規作成
 */

package com.example.bookapp03.ui;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookapp03.R;
import com.example.bookapp03.data.model.BookDetailData; // パッケージ変更
import com.example.bookapp03.logic.BookDetailViewController;
import com.example.bookapp03.presentation.viewmodel.BookDetailViewModel; // ViewModelのパッケージ変更

/**
 * 書籍詳細画面を表示するアクティビティ
 */
public class BookDetailActivity extends AppCompatActivity {

    private BookDetailViewController controller;
    private BookDetailViewModel viewModel;
    private String currentVolumeId; // 現在表示している書籍のvolumeIdを保持

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        controller = new BookDetailViewController();
        View rootView = findViewById(android.R.id.content); // Activityのルートビューを取得

        // ViewModelProviderを使用してViewModelを初期化
        viewModel = new ViewModelProvider(this, new ViewModelFactory(getApplicationContext()))
                .get(BookDetailViewModel.class);

        // インテントからvolumeIdを取得
        currentVolumeId = getIntent().getStringExtra("volumeId");

        // TODO: 実際のユーザーUIDを取得するロジックをここに実装
        String currentUserId = "user123"; // 仮のユーザーID

        // LiveDataの変更を監視
        viewModel.bookDetail.observe(this, bookDetailData -> {
            // データが更新されたらUIを更新
            boolean showNoBookMessage = (currentVolumeId == null || currentVolumeId.isEmpty()) || (bookDetailData == null);
            controller.displayBookDetails(bookDetailData, rootView, showNoBookMessage);

            // ハイライトメモもロード
            if (bookDetailData != null && bookDetailData.getVolumeId() != null) {
                // ボトムシートコントローラがあればここでメモ表示ロジックも呼び出す
                // 例: new HighlightMemoBottomSheetController().displayMemo(rootView, bookDetailData.getSummary()); // 仮に全体まとめをメモとして表示
                viewModel.loadHighlightMemos(currentUserId, bookDetailData.getVolumeId());
            }
        });

        // Activityが作成されたらViewModelにデータのロードを指示
        if (currentVolumeId != null && !currentVolumeId.isEmpty()) {
            viewModel.loadBookDetail(currentUserId, currentVolumeId);
        } else {
            // volumeIdがない場合は「該当書籍なし」メッセージを表示
            controller.displayBookDetails(null, rootView, true);
        }

        // ここからハイライトメモの表示をトリガーする例
        // 例えば、ボタンがクリックされたらボトムシートを表示する
        // findViewById(R.id.show_memos_button).setOnClickListener(v -> {
        //     // ViewModelからハイライトメモを取得し、ボトムシートに渡す
        //     List<HighlightMemoData> memos = viewModel.highlightMemos.getValue();
        //     new HighlightMemoBottomSheetController().controlSheetDisplay(memos, true); // ボトムシート表示メソッド
        // });
    }
}