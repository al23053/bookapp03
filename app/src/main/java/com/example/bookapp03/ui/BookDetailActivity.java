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

import com.example.bookapp03.data.BookDetailData;
import com.example.bookapp03.logic.BookDetailViewController;
import com.example.bookapp03.data.BookDetailViewModel;
import com.example.bookapp03.R;

/**
 * 書籍詳細画面を表示するアクティビティ
 */
public class BookDetailActivity extends AppCompatActivity {

    /**
     * 書籍詳細の表示制御を行うコントローラー
     */
    private BookDetailViewController controller = new BookDetailViewController();

    /**
     * 書籍詳細データの保持および処理を行うViewModel
     */
    private BookDetailViewModel viewModel = new BookDetailViewModel();

    /**
     * アクティビティ生成時の処理。インテントから書籍データを取得し、画面に表示する。
     *
     * @param savedInstanceState 前回保存されたインスタンスの状態（未使用）
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail); // レイアウトファイルは事前に作成済みであること

        // インテントから書籍情報を取得
        String volumeId = getIntent().getStringExtra("volumeId");
        String name = getIntent().getStringExtra("name");
        String summary = getIntent().getStringExtra("summary");
        String coverImageUrl = getIntent().getStringExtra("coverImageUrl");
        String publicStatus = getIntent().getStringExtra("publicStatus");

        // 取得した情報でデータオブジェクトを生成
        BookDetailData data = new BookDetailData(volumeId, name, summary, coverImageUrl, publicStatus);

        // ViewModelにデータを設定
        viewModel.setDetail(data);

        // 表示制御クラスにデータとルートビューを渡す
        View rootView = findViewById(android.R.id.content);
        controller.displayBookDetails(viewModel.getDetail(), rootView, true);
    }
}