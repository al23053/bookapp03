/**
 * モジュール名: MainActivity
 * 作成者: 増田学斗
 * 作成日: 2025/06/15
 * 概要: メインのプログラム
 * 履歴:
 *   2025/06/15 増田学斗 新規作成
 */

package com.example.bookapp03.C1UIProcessing;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.bookapp03.R;


public class MainActivity extends Activity {

    /** Googleログインのクライアントオブジェクト */
    private GoogleSignInClient googleSignInClient;

    /** Firebase Authentication のインスタンス */
    private FirebaseAuth mAuth;

    /** Googleログイン時のリクエストコード */
    private static final int RC_SIGN_IN = 1000;

    /**
     * アクティビティの初期化処理。Googleログインの設定とボタン処理を行う。
     *
     * @param savedInstanceState アクティビティの保存状態
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Googleサインインのオプション設定（Firebase連携用）
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Firebase用WebクライアントID
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        // ログインボタンを押したときにGoogleサインイン画面を表示
        Button loginButton = findViewById(R.id.to_login_button);
        loginButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    /**
     * Googleログイン結果を受け取り、Firebase認証を実施する。
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
                Toast.makeText(this, "Googleサインイン失敗", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * FirebaseにGoogleアカウントで認証する。
     *
     * @param acct Googleアカウント
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkIfUserExistsAndProceed(user);
                    } else {
                        Toast.makeText(this, "Firebase認証失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Firebase上に既存のユーザーデータがあるか確認し、
     * あればホーム画面、なければ登録処理後に登録完了画面へ遷移する。
     *
     * @param user Firebase認証済みユーザー
     */
    private void checkIfUserExistsAndProceed(FirebaseUser user) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // すでに登録済み → ホーム画面へ遷移
                startActivity(new Intent(this, HomeActivity.class));
            } else {
                // 未登録 → ユーザー情報を登録し登録完了画面へ遷移
                userRef.setValue(new UID(user.getEmail(), user.getDisplayName()))
                        .addOnSuccessListener(unused -> {
                            startActivity(new Intent(this, RegistrationDoneActivity.class));
                        });
            }
        });
    }
}
