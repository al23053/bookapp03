/**
 * モジュール名: LoginActivity
 * 作成者: 増田学斗
 * 作成日: 2025/06/15
 * 概要: Googleアカウントによるログイン処理と、Firebase Authentication 認証、
 *       Firestore におけるユーザー情報の確認・新規登録を行う。
 *       UIDを端末に保存する処理も追加されている。
 * 履歴:
 *   2025/06/15 増田学斗 新規作成
 *   2025/07/06 鶴田凌 UIDを端末に保存する処理を追加
 */
package com.example.bookapp03.C1UIProcessing;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp03.C2UserInformationProcessing.LocalAccountStore;
import com.example.bookapp03.R;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    /**
     * Googleサインイン クライアント
     */
    private GoogleSignInClient googleSignInClient;

    /**
     * Firebase Authentication
     */
    private FirebaseAuth mAuth;

    /**
     * サインインリクエストコード
     */
    private static final int RC_SIGN_IN = 1000;

    /**
     * アクティビティ初期化処理
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase 認証初期化
        mAuth = FirebaseAuth.getInstance();

        // Google サインインオプション設定
        initializeGoogleSignIn();

        // ログインボタン設定
        setupLoginButton();
    }

    /**
     * Googleサインインのクライアントを初期化
     */
    private void initializeGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    /**
     * 「ログイン」ボタン押下時の処理を設定
     */
    private void setupLoginButton() {
        Button loginButton = findViewById(R.id.to_login_button);
        loginButton.setOnClickListener(view -> {
            // 一度ログアウトしてから再度ログイン（キャッシュ無視のため）
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });
        });
    }

    /**
     * Google サインインの結果を受け取って処理する
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
                Log.e(TAG, "Googleサインイン失敗", e);
                Toast.makeText(this, "Googleサインイン失敗", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Google アカウントを使用して Firebase に認証する
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkUserInFirestore(user);
                    } else {
                        Log.e(TAG, "Firebase認証失敗", task.getException());
                        Toast.makeText(this, "Firebase認証失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Firestoreにユーザー情報が存在するか確認し、なければ新規登録を行う。
     * 登録済みの場合はホーム画面へ、新規登録時は登録完了画面へ遷移。
     *
     * @param user 認証済みのFirebaseユーザー
     */
    private void checkUserInFirestore(FirebaseUser user) {
        String uid = user.getUid();
        Log.d(TAG, "ユーザー取得成功: " + uid);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Firestore読み込み失敗", task.getException());
                return;
            }

            if (task.getResult().exists()) {
                // 既存ユーザー → UIDを保存してホームへ
                Log.d(TAG, "ユーザーは登録済み");
                LocalAccountStore.addUid(this, uid);
                startActivity(new Intent(this, DisplayHome.class));
                finish();
            } else {
                // 初回ログイン → Firestoreに情報登録
                Log.d(TAG, "初回ログイン。ユーザー情報を登録中...");
                registerNewUser(uid, user.getEmail(), user.getDisplayName());
            }
        });
    }

    /**
     * Firestoreに新しいユーザー情報を登録し、完了画面へ遷移
     */
    private void registerNewUser(String uid, String email, String displayName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("displayName", displayName);

        db.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener(unused -> {
                    // 登録成功 → UID保存 → 完了画面へ
                    LocalAccountStore.addUid(this, uid);
                    Log.d(TAG, "ユーザー情報登録成功 → 登録完了画面へ");
                    Intent intent = new Intent(this, RegistrationDoneActivity.class);
                    intent.putExtra("next", "AccountSettingActivity");
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "ユーザー登録失敗", e);
                    Toast.makeText(this, "ユーザー登録に失敗しました", Toast.LENGTH_SHORT).show();
                });
    }
}
