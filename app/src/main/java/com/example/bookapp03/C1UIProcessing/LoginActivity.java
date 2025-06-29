
/**
 * モジュール名: LoginActivity
 * 作成者: 増田学斗
 * 作成日: 2025/06/15
 * 概要: ログイン処理（Firestore対応）
 * 履歴:
 *   2025/06/15 増田学斗 新規作成
 */
package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp03.R;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    /** Googleログイン用のクライアントインスタンス */
    private GoogleSignInClient googleSignInClient;

    /** サインイン処理用のリクエストコード */
    private static final int RC_SIGN_IN = 1000;

    /** Firebase Authentication のインスタンス */
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        Button loginButton = findViewById(R.id.to_login_button);
        loginButton.setOnClickListener(view -> {
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });
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
                        checkUserInFirestore(user);
                    } else {
                        Toast.makeText(this, "Firebase認証失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Firestoreにユーザー情報が存在するか確認し、なければ新規登録する。
     *
     * @param user Firebaseで認証されたユーザー
     */
    private void checkUserInFirestore(FirebaseUser user) {
        Log.d("LoginDebug", "ユーザー取得成功: " + user.getUid());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = user.getUid();

        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("LoginDebug", "Firestore読み込み失敗", task.getException());
                    } else if (task.getResult().exists()) {
                        Log.d("LoginDebug", "ユーザーは登録済み");
                        startActivity(new Intent(this, DisplayHome.class));
                    } else {
                        Log.d("LoginDebug", "初回ログイン。ユーザー情報を登録中...");

                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("email", user.getEmail());
                        userMap.put("displayName", user.getDisplayName());

                        db.collection("users").document(uid)
                                .set(userMap)
                                .addOnSuccessListener(unused -> {
                                    Log.d("LoginDebug", "ユーザー情報を登録 → 登録完了画面へ");
                                    Intent intent = new Intent(this, RegistrationDoneActivity.class);
                                    intent.putExtra("next", "AccountSettingActivity");
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("LoginDebug", "登録に失敗", e);
                                });
                    }
                });
    }
}
