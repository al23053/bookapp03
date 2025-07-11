/**
 * モジュール名: GenreSelectionActivity
 * 作成者: 増田学斗
 * 作成日: 2025/06/15
 * 概要: ユーザーが本のジャンルを選択する画面。選択内容をFirestoreに保存し、次画面に進む。
 *       Firestoreに保存済みのジャンルがある場合は、そのチェック状態を復元する。
 * 履歴:
 *   2025/06/15 増田学斗 新規作成
 *   2025/07/07 増田学斗 Firestoreからジャンル情報を読み込みチェック状態を復元する機能を追加
 */
package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp03.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GenreSelectionActivity extends AppCompatActivity {

    private static final String TAG = "GenreSelectionActivity";

    /**
     * 初回設定かどうかを判定するフラグ
     */
    private boolean isFirstTime = true;

    /**
     * ジャンル選択用チェックボックス群
     */
    private ArrayList<CheckBox> genreCheckboxes = new ArrayList<>();

    /**
     * 前画面から受け取るニックネーム
     */
    private String nickname;

    /**
     * 前画面から受け取るアイコン画像（Base64形式）
     */
    private String iconBase64;

    /**
     * 「次へ」ボタン
     */
    private Button buttonNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre_selection);

        Log.d(TAG, "onCreate開始");

        // 前画面からのデータを取得
        retrieveIntentData();

        // View 初期化
        initializeViews();

        // Firestoreから保存済みジャンルを読み込む
        loadExistingGenres();

        // 「次へ」ボタンの処理設定
        setupNextButton();
    }

    /**
     * Intentからnickname, iconBase64, isFirstTimeを取得
     */
    private void retrieveIntentData() {
        nickname = getIntent().getStringExtra("nickname");
        iconBase64 = getIntent().getStringExtra("iconBase64");
        isFirstTime = getIntent().getBooleanExtra("isFirstTime", true);
    }

    /**
     * View要素の初期化
     */
    private void initializeViews() {
        genreCheckboxes.add(findViewById(R.id.checkbox_mystery));
        genreCheckboxes.add(findViewById(R.id.checkbox_horror));
        genreCheckboxes.add(findViewById(R.id.checkbox_biography));
        genreCheckboxes.add(findViewById(R.id.checkbox_selfhelp));
        genreCheckboxes.add(findViewById(R.id.checkbox_romance));
        genreCheckboxes.add(findViewById(R.id.checkbox_history));
        genreCheckboxes.add(findViewById(R.id.checkbox_business));
        genreCheckboxes.add(findViewById(R.id.checkbox_scifi));
        genreCheckboxes.add(findViewById(R.id.checkbox_fantasy));
        genreCheckboxes.add(findViewById(R.id.checkbox_technology));

        buttonNext = findViewById(R.id.buttonNextToComplete);
    }

    /**
     * 「次へ」ボタンのクリック処理を設定
     */
    private void setupNextButton() {
        buttonNext.setOnClickListener(v -> {
            ArrayList<String> selectedGenres = new ArrayList<>();

            for (CheckBox cb : genreCheckboxes) {
                if (cb.isChecked()) {
                    selectedGenres.add(cb.getText().toString());
                }
            }

            if (selectedGenres.isEmpty()) {
                Toast.makeText(this, "1つ以上選択してください", Toast.LENGTH_SHORT).show();
                return;
            }

            saveGenresToFirestore(selectedGenres);
        });
    }

    /**
     * Firestoreにジャンル情報を保存し、次画面に進む
     */
    private void saveGenresToFirestore(List<String> selectedGenres) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        HashMap<String, Object> userData = new HashMap<>();
        userData.put("nickname", nickname);
        userData.put("iconBase64", iconBase64);
        userData.put("genre", selectedGenres);

        db.collection("users").document(uid)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "ユーザ情報の保存に成功");

                    if (isFirstTime) {
                        // 初回設定時：完了画面に遷移
                        Intent intent = new Intent(this, CompleteActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // 再設定時：元の画面に戻る
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore保存失敗", e);
                    Toast.makeText(this, "登録失敗", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Firestoreから既存のジャンルを取得し、チェックボックスに反映
     */
    private void loadExistingGenres() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get().addOnSuccessListener(document -> {
            if (document.exists()) {
                List<String> savedGenres = (List<String>) document.get("genre");
                if (savedGenres != null) {
                    for (CheckBox cb : genreCheckboxes) {
                        if (savedGenres.contains(cb.getText().toString())) {
                            cb.setChecked(true);
                        }
                    }
                    Log.d(TAG, "既存ジャンルの復元成功");
                }
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "ジャンル読み込み失敗", e);
        });
    }
}
