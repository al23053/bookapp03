/**
 * モジュール名: CompleteActivity
 * 作成者: 増田学斗
 * 作成日: 2025/06/15
 * 概要: ユーザー情報登録完了を知らせる画面。メッセージを表示し、ホーム画面へ遷移する。
 * 履歴:
 * 2025/06/15 増田学斗 新規作成
 */
package com.example.bookapp03.C1UIProcessing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp03.R;

public class CompleteActivity extends AppCompatActivity {

    /**
     * 完了メッセージ表示用テキストビュー
     */
    private TextView textViewMessage;

    /**
     * ホーム画面に遷移するボタン
     */
    private Button buttonToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete);

        // View 初期化
        initializeViews();

        // 完了メッセージを表示
        showCompleteMessage();

        // 「ホームへ」ボタン押下時の遷移処理を設定
        setupButtonToHome();
    }

    /**
     * View 要素を初期化する
     */
    private void initializeViews() {
        textViewMessage = findViewById(R.id.textViewCompleteMessage);
        buttonToHome = findViewById(R.id.buttonToHome);
    }

    /**
     * 登録完了メッセージを表示する
     */
    private void showCompleteMessage() {
        textViewMessage.setText("ユーザ情報の登録完了！");
    }

    /**
     * 「ホームへ」ボタン押下時の処理を設定する
     */
    private void setupButtonToHome() {
        buttonToHome.setOnClickListener(v -> {
            // ホーム画面に遷移
            Intent intent = new Intent(this, DisplayHome.class);
            startActivity(intent);
            finish(); // 完了画面を閉じる
        });
    }
}
