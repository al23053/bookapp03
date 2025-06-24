/**
 * ホーム画面を表示するアクティビティ。
 * Firebase Authentication を使ってログインユーザーを取得し、
 * ユーザー名を含んだ挨拶メッセージを画面に表示する。
 * <p>
 * 作成者: 増田学斗
 * 作成日: 2025/06/15
 */
package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.bookapp03.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class HomeActivity extends Activity {

    /**
     * アクティビティの初期化処理。
     * ユーザーがログイン済みであれば挨拶メッセージを表示し、
     * そうでなければログインを促す。
     *
     * @param savedInstanceState アクティビティの保存状態
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 挨拶メッセージを表示するTextViewを取得
        TextView welcomeText = findViewById(R.id.home_text);

        // Firebaseから現在のログインユーザーを取得
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // 表示名が取得できる場合はそれを使用
            String name = user.getDisplayName();
            welcomeText.setText("ようこそ、" + (name != null ? name : "ユーザー") + "さん！");
        } else {
            // ログインしていない場合のメッセージ
            welcomeText.setText("ログインしてください");
        }
    }
}
