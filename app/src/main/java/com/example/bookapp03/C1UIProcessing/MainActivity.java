/**
 * モジュール名: MainActivity
 * 作成者: 増田学斗
 * 作成日: 2025/06/15
 * 概要: GoogleサインインとFirestoreユーザー情報処理を行うメインアクティビティ
 * 履歴:
 * 2025/06/15 増田学斗 新規作成
 * 2025/06/27 増田学斗 Firestoreに移行
 */

package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
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

/**
 * メインアクティビティ（ログインとFirestoreへのユーザー情報登録を行う）
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Googleサインインのクライアント
     */
    private GoogleSignInClient googleSignInClient;

    /**
     * Firebase Authenticationインスタンス
     */
    private FirebaseAuth mAuth;

    /**
     * Googleサインインのリクエストコード
     */
    private static final int RC_SIGN_IN = 1000;

    /**
     * アクティビティの初期化処理
     *
     * @param savedInstanceState 保存されたインスタンス状態（未使用）
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Googleサインインの設定を構築
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        // ログインボタン処理
        Button loginButton = findViewById(R.id.to_login_button);
        loginButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    /**
     * Googleサインインからの戻り結果を処理
     *
     * @param requestCode リクエストコード
     * @param resultCode  結果コード
     * @param data        戻りインテントデータ
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
     * Firebase認証をGoogleアカウントで行う
     *
     * @param acct サインインしたGoogleアカウント
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                checkIfUserExistsAndProceed(user);
            } else {
                Toast.makeText(this, "Firebase認証失敗", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Firestoreにユーザー情報が存在するか確認し、次の画面へ遷移
     *
     * @param user 現在ログイン中のFirebaseユーザー
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
                // 新規ユーザー → Firestoreに最低限の情報を保存して登録完了画面へ
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("email", user.getEmail());
                userMap.put("displayName", user.getDisplayName());
                userMap.put("nickname", ""); // 後で設定
                userMap.put("iconUri", "");  // 後で設定

                userRef.set(userMap).addOnSuccessListener(unused -> {
                    startActivity(new Intent(this, RegistrationDoneActivity.class));
                    finish();
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "ユーザー情報の保存に失敗しました", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
