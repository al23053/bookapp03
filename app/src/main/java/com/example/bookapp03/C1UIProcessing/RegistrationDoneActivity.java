/**
 * モジュール名: RegistrationDoneActivity
 * 作成者: 増田学斗
 * 作成日: 2025/06/15
 * 概要: googleカウント登録完了画面の処理
 * 履歴:
 *   2025/06/15 増田学斗 新規作成
 */

package com.example.a1bapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import android.util.Log;

import com.example.a1bapp.C1UIProcessing.AccountSettingActivity;

public class RegistrationDoneActivity extends Activity {

    /**
     * アクティビティの初期化処理。
     * 登録完了メッセージを表示し、「次へ」ボタンでAccountSettingActivityに遷移する。
     *
     * @param savedInstanceState アクティビティの保存状態
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("AccountSettingDebug", "onCreate called");
        setContentView(R.layout.activity_registration_done);

        // 完了メッセージをTextViewに設定
        TextView message = findViewById(R.id.registration_message);
        message.setText("アカウントの登録が完了しました！");

        // 「次へ」ボタンを取得してクリック処理を設定
        Button nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(view -> {
            // アカウント設定画面へ遷移
            Intent intent = new Intent(RegistrationDoneActivity.this, AccountSettingActivity.class);
            startActivity(intent);

            // この画面には戻らないようにfinishを呼び出す
            finish();
        });
    }
}
