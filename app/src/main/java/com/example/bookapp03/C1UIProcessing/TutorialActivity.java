/**
 * モジュール名: 使い方画面表示処理
 * 作成者: 横山葉
 * 作成日: 2025/06/20
 * 概要: アプリケーションのチュートリアル画像を順に表示するアクティビティ。
 * 履歴:
 * 2025/06/20 横山葉 新規作成
 */
package com.example.bookapp03.C1UIProcessing;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookapp03.R;

/**
 * チュートリアル画像を順番に表示し、ユーザーにアプリの使い方を案内するアクティビティ。
 */
public class TutorialActivity extends AppCompatActivity {

    /** 表示するチュートリアル画像リスト */
    private final int[] tutorialImages = {
            R.drawable.tutorial_1,
            R.drawable.tutorial_2,
            R.drawable.tutorial_3,
            R.drawable.tutorial_4
    };

    /** 現在のチュートリアルページインデックス */
    private int currentIndex = 0;

    /** チュートリアル画像を表示するImageView */
    private ImageView tutorialImageView;
    /** 次へ／閉じるボタン */
    private Button nextButton;
    /** スキップボタン */
    private Button skipButton;

    /**
     * チュートリアル画面初期化処理
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_images);

        tutorialImageView = findViewById(R.id.tutorial_image_view);
        nextButton = findViewById(R.id.next_button);
        skipButton = findViewById(R.id.skip_button);

        showImage();

        nextButton.setOnClickListener(v -> {
            currentIndex++;
            if (currentIndex >= tutorialImages.length) {
                finishTutorial();
            } else {
                showImage();
            }
        });

        skipButton.setOnClickListener(v -> finishTutorial());
    }

    /**
     * 現在のチュートリアル画像を表示する
     */
    private void showImage() {
        tutorialImageView.setImageResource(tutorialImages[currentIndex]);
        nextButton.setText(currentIndex == tutorialImages.length - 1 ? "閉じる" : "次へ");
    }

    /**
     * チュートリアルを終了して画面を閉じる
     */
    private void finishTutorial() {
        new TutorialManager(this).markTutorialAsShown();
        finish();
    }
}
