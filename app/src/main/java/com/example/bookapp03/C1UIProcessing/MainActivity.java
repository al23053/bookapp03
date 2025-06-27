/**
 * モジュール名: MainActivity
 * 作成者: 増田学斗
 * 作成日: 2025/06/15
 * 概要: メインのプログラム（Firestore対応版）
 * 履歴:
 *   2025/06/15 増田学斗 新規作成
 *   2025/06/27 増田学斗 Firestoreに移行
 */

package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.bookapp03.R;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Googleサインインの設定
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        // ログインボタン処理
        Button loginButton = findViewById(R.id.to_login_button);
        loginButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

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

    private void checkIfUserExistsAndProceed(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(user.getUid());

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // すでにユーザー情報がある → ホーム画面へ
                startActivity(new Intent(this, DisplayHome.class));
                finish();
            } else {
                // 未登録 → 最低限の情報をFirestoreに保存し、登録完了画面へ
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("email", user.getEmail());
                userMap.put("displayName", user.getDisplayName());
                userMap.put("nickname", ""); // 後で設定
                userMap.put("iconUri", "");  // 後で設定

                userRef.set(userMap)
                        .addOnSuccessListener(unused -> {
                            startActivity(new Intent(this, RegistrationDoneActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "ユーザー情報の保存に失敗しました", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}
