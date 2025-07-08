/**
 * モジュール名: RegistrationDoneActivity
 * 作成者: 増田学斗
 * 作成日: 2025/06/15
 * 概要: Googleアカウントの初回登録完了画面。登録完了メッセージを表示し、
 *       「次へ」ボタンでアカウント設定画面（AccountSettingActivity）へ遷移。
 *       現在のUIDをローカルストレージに保存する。
 * 履歴:
 *   2025/06/15 増田学斗 新規作成
 */
package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.bookapp03.C2UserInformationProcessing.LocalAccountStore;
import com.example.bookapp03.C5UserInformationManaging.UserAuthManager;
import com.example.bookapp03.R;

public class RegistrationDoneActivity extends Activity {

    /**
     * 登録完了メッセージを表示する TextView
     */
    private TextView messageTextView;

    /**
     * 「次へ」ボタン（アカウント設定画面へ遷移）
     */
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_done);

        // View 初期化
        initializeViews();

        // 登録完了メッセージ表示
        showCompletionMessage();

        // 「次へ」ボタンの遷移処理設定
        setupNextButton();
    }

    /**
     * View 要素を初期化
     */
    private void initializeViews() {
        messageTextView = findViewById(R.id.registration_message);
        nextButton = findViewById(R.id.next_button);
    }

    /**
     * 登録完了メッセージを TextView に設定
     */
    private void showCompletionMessage() {
        messageTextView.setText("アカウントの登録が完了しました！");
    }

    /**
     * 「次へ」ボタン押下時の処理を設定
     * 現在のUIDをローカルストレージに保存し、AccountSettingActivityへ遷移
     */
    private void setupNextButton() {
        nextButton.setOnClickListener(view -> {
            // 現在ログイン中のUIDを取得
            String uid = UserAuthManager.getCurrentUid();

            // UIDをローカルに保存
            LocalAccountStore.addUid(this, uid);

            // アカウント設定画面へ遷移
            Intent intent = new Intent(this, AccountSettingActivity.class);
            startActivity(intent);

            // この画面は閉じる（戻れないように）
            finish();
        });
    }
}
