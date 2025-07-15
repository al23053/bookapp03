/**
 * モジュール名: NoReviewsActivity
 * 作成者: 三浦寛生
 * 作成日: 2025/06/15
 * 概要:書籍に全体まとめがまだ登録されていない場合に表示されるActivityです。
 * ユーザーにその旨を伝え、前の画面に戻るためのボタンを提供します。
 * 履歴:
 * 2025/06/15 三浦寛生 新規作成
 */
package com.example.bookapp03.C1UIProcessing;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;

import com.example.bookapp03.R;

public class NoReviewsActivity extends AppCompatActivity {

    /**
     * Activityが最初に作成されるときに呼び出されます。
     * レイアウトの設定、UI要素の初期化、そして前の画面から渡された書籍情報の表示を行います。
     *
     * @param savedInstanceState 以前に保存された状態データを含むBundleオブジェクト。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_reviews);

        TextView messageTextView = findViewById(R.id.noReviewsMessage);
        Button backButton = findViewById(R.id.noReviewsBackButton);

        String bookTitle = getIntent().getStringExtra("bookTitle");

        if (bookTitle != null) {
            messageTextView.setText(bookTitle + " にはまだ全体まとめが登録されていません。");
        } else {
            messageTextView.setText("この本にはまだ全体まとめが登録されていません。");
        }

        ControlBackButton.setupBackButton(backButton, this);
    }
}
