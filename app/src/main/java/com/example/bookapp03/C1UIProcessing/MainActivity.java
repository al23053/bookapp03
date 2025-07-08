/**
 * モジュール名: MainActivity
 * 作成者: 増田学斗
 * 作成日: 2025/06/15
 * 概要: GoogleサインインおよびFirebase Authenticationを用いたログイン処理を行う。
 *       初回ログイン時にはFirestoreにユーザー情報を保存し、登録完了画面へ遷移する。
 * 履歴:
 *   2025/06/15 増田学斗 新規作成
 *   2025/06/27 増田学斗 Firestore対応に改修
 */
package com.example.bookapp03.C1UIProcessing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp03.R;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    /**
     * Firebase Authentication インスタンス
     */
    private FirebaseAuth mAuth;

    /**
     * Googleサインイン クライアント
     */
    private GoogleSignInClient googleSignInClient;

    /**
     * Googleサインイン用のリクエストコード
     */
    private static final int RC_SIGN_IN = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // FirebaseAuth インスタンスを初期化
        mAuth = FirebaseAuth.getInstance();

        // Googleサインインオプションとクライアントを初期化
        initializeGoogleSignIn();

        // すでにログイン済みのユーザーがいる場合は即座に処理
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkIfUserExistsAndProceed(currentUser);
            return;
        }

        // 未ログインの場合はログイン画面を表示
        setContentView(R.layout.activity_login);
        setupLoginButton();
    }

    /**
     * Googleサインイン用のオプションとクライアントを設定
     */
    private void initializeGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    /**
     * ログインボタン押下時の処理を設定
     */
    private void setupLoginButton() {
        Button loginButton = findViewById(R.id.to_login_button);
        loginButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    /**
     * Googleサインインの結果を受け取る
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
                Toast.makeText(this, "Googleサインイン失敗", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    /**
     * FirebaseにGoogleアカウントで認証を行う
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
     * Firestore上にユーザー情報があるか確認し、次の画面へ遷移する
     */
    private void checkIfUserExistsAndProceed(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(user.getUid());

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // 既存ユーザー → ホーム画面へ
                startActivity(new Intent(this, DisplayHome.class));
                finish();
            } else {
                // 新規ユーザー → Firestoreに情報を登録し、完了画面へ
                registerNewUserToFirestore(user);
            }
        });
    }

    /**
     * 新規ユーザー情報をFirestoreに登録して、登録完了画面へ遷移
     */
    private void registerNewUserToFirestore(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(user.getUid());

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", user.getEmail());
        userMap.put("uid", user.getUid());
        userMap.put("nickname", ""); // 後で AccountSettingActivity で入力
        userMap.put("iconUri", "");  // 後で AccountSettingActivity で設定

        userRef.set(userMap).addOnSuccessListener(unused -> {
            // 完了画面に遷移
            Intent intent = new Intent(this, RegistrationDoneActivity.class);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "ユーザー情報の保存に失敗しました", Toast.LENGTH_SHORT).show();
        });
    }
}
