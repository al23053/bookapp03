/**
 * モジュール名: CompleteActivity
 * 作成者: 増田学斗
 * 作成日: 2025/06/15
 * 概要: ユーザ情報登録完了画面の処理
 * 履歴:
 *   2025/06/15 増田学斗 新規作成
 */
package com.example.a1bapp.C1UIProcessing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.a1bapp.HomeActivity;
import com.example.a1bapp.R;

public class CompleteActivity extends Activity {

    /**
     * アクティビティ起動時の初期化処理。
     * 完了メッセージの表示と、ボタン押下時の画面遷移を設定する。
     *
     * @param savedInstanceState アクティビティの保存状態
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete);

        // 完了メッセージを表示する TextView を取得して設定
        TextView textViewMessage = findViewById(R.id.textViewCompleteMessage);
        textViewMessage.setText("ユーザ情報の登録完了！");

        // 「ホームへ」ボタンを取得し、クリックで HomeActivity に遷移
        Button buttonToHome = findViewById(R.id.buttonToHome);
        buttonToHome.setOnClickListener(v -> {
            // ホーム画面に遷移するIntentを作成し起動
            Intent intent = new Intent(CompleteActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
