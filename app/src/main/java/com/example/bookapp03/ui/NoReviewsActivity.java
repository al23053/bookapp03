package com.example.bookapp03.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;

import com.example.bookapp03.C1UIProcessing.ControlBackButton;
import com.example.bookapp03.R; // あなたのRクラスのパス

/**
 * 書籍にレビュー（全体まとめ）がまだ登録されていない場合に表示されるActivityです。
 * ユーザーにその旨を伝え、前の画面に戻るためのボタンを提供します。
 */
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

        // UI要素の参照を取得
        TextView messageTextView = findViewById(R.id.noReviewsMessage);
        Button backButton = findViewById(R.id.noReviewsBackButton);

        // 前の画面から本のタイトル情報を受け取る
        String bookTitle = getIntent().getStringExtra("bookTitle");

        if (bookTitle != null) {
            messageTextView.setText(bookTitle + " にはまだ全体まとめが登録されていません。");
        } else {
            // 書籍タイトルが渡されなかった場合のメッセージ
            messageTextView.setText("この本にはまだ全体まとめが登録されていません。");
        }

        // 「戻る」ボタンのクリックリスナーを設定
        // このActivityを終了して前の画面に戻ります。
        ControlBackButton.setupBackButton(backButton, this);
    }
}
