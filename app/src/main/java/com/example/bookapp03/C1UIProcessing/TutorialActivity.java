/**
 * モジュール名: 使い方画面表示処理
 * 作成者: 横山葉
 * 作成日: 2025/06/20
 * 概要: アプリケーションのチュートリアルを表示するアクティビティ。
 * 履歴:
 * 2025/06/20 横山葉 新規作成
 */
package com.example.bookapp03.C1UIProcessing;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookapp03.R;

/**
 * アプリケーションの初期起動時に表示されるチュートリアル画面を管理するアクティビティ。
 * 複数のページを順に表示し、スキップまたは完了の操作を受け付けます。
 */
public class TutorialActivity extends AppCompatActivity {
    /** チュートリアルページの総数 */
    private int totalPages = 3;
    /** 現在表示しているチュートリアルページのインデックス（0から始まる） */
    private int currentIndex = 0;

    /** チュートリアル内容を表示するTextView */
    private TextView dummyText;
    /** 次のページへ進む、またはチュートリアルを閉じるボタン */
    private Button nextButton;
    /** チュートリアルをスキップするボタン */
    private Button skipButton;

    /**
     * アクティビティが最初に作成されたときに呼び出される。
     * UIコンポーネントの初期化、ページ表示、およびボタンのクリックリスナーを設定する。
     *
     * @param savedInstanceState アクティビティの以前の保存状態を含むBundleオブジェクト。
     * アクティビティが以前に存在し、最後に終了していなかった場合に非nullとなる。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_dummy);

        // UIコンポーネントの参照を取得
        dummyText = findViewById(R.id.dummy_text);
        nextButton = findViewById(R.id.next_button);
        skipButton = findViewById(R.id.skip_button);

        // 最初のページを表示
        showPage();

        // 「次へ」または「閉じる」ボタンのクリックリスナーを設定
        nextButton.setOnClickListener(v -> {
            currentIndex++; // 現在のページインデックスをインクリメント
            // 全てのページを表示し終えたかを確認
            if (currentIndex >= totalPages) {
                finishTutorial(); // チュートリアルを終了
            } else {
                showPage(); // 次のページを表示
            }
        });

        // 「スキップ」ボタンのクリックリスナーを設定
        skipButton.setOnClickListener(v -> finishTutorial()); // チュートリアルを終了
    }

    /**
     * 現在のインデックスに基づいてチュートリアルページの内容を更新し、
     * 「次へ」ボタンのテキストを調整する。
     */
    private void showPage() {
        // 現在のページ番号と総ページ数を表示
        dummyText.setText("チュートリアル " + (currentIndex + 1) + " / " + totalPages);
        // 最終ページの場合、「閉じる」ボタン、それ以外は「次へ」ボタンを表示
        nextButton.setText(currentIndex == totalPages - 1 ? "閉じる" : "次へ");
    }

    /**
     * チュートリアルを終了する処理。
     * チュートリアルが既に表示されたことを記録し、アクティビティを終了する。
     */
    private void finishTutorial() {
        // チュートリアルが完了したことをマーク
        new TutorialManager(this).markTutorialAsShown();
        // 現在のアクティビティを終了
        finish();
    }
}