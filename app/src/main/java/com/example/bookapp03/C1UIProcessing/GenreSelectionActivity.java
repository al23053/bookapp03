/**
 * モジュール名: GenreSelectionActivity
 * 作成者: 増田学斗
 * 作成日: 2025/06/15
 * 概要: 本のジャンル選択処理
 * 履歴:
 *   2025/06/15 増田学斗 新規作成
 */
package com.example.a1bapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.a1bapp.C1UIProcessing.CompleteActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class GenreSelectionActivity extends Activity {

    /** ジャンル選択用のチェックボックスをまとめるリスト */
    private ArrayList<CheckBox> genreCheckboxes = new ArrayList<>();

    /** 前画面から受け取るニックネーム */
    private String nickname;

    /** 前画面から受け取るアイコン画像URI文字列 */
    private String iconUri;

    /**
     * アクティビティの初期化処理。
     * チェックボックスとボタンのリスナー登録、Intentデータの取得を行う。
     *
     * @param savedInstanceState 保存されたインスタンスの状態
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("GenreSelection", "onCreate called");
        setContentView(R.layout.activity_genre_selection);

        // Intentから受け取るデータ（ニックネームとアイコンURI）
        nickname = getIntent().getStringExtra("nickname");
        iconUri = getIntent().getStringExtra("iconUri"); // ← ここは iconBase64 ではなく iconUri に揃える

        // 各ジャンルのチェックボックスをリストに追加
        genreCheckboxes.add(findViewById(R.id.checkbox_mystery));
        genreCheckboxes.add(findViewById(R.id.checkbox_horror));
        genreCheckboxes.add(findViewById(R.id.checkbox_biography));
        genreCheckboxes.add(findViewById(R.id.checkbox_selfhelp));
        genreCheckboxes.add(findViewById(R.id.checkbox_romance));

        // 「次へ」ボタン押下時の処理
        Button buttonNext = findViewById(R.id.buttonNextToComplete);
        buttonNext.setOnClickListener(v -> {
            ArrayList<String> selectedGenres = new ArrayList<>();

            // 選択されたジャンルをリストに追加
            for (CheckBox cb : genreCheckboxes) {
                if (cb.isChecked()) {
                    selectedGenres.add(cb.getText().toString());
                }
            }

            // ジャンルが1つも選択されていない場合は警告
            if (selectedGenres.isEmpty()) {
                Toast.makeText(this, "1つ以上選択してください", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebaseへユーザー情報を保存
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            HashMap<String, Object> userData = new HashMap<>();
            userData.put("nickname", nickname);
            userData.put("genre", selectedGenres);
            userData.put("iconUri", iconUri);

            FirebaseDatabase.getInstance().getReference("users")
                    .child(uid)
                    .setValue(userData)
                    .addOnSuccessListener(unused -> {
                        Log.d("GenreSelection", "ユーザ情報の保存に成功");

                        // 完了画面に遷移
                        Intent intent = new Intent(this, CompleteActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("GenreSelection", "ユーザ情報の保存に失敗", e);
                        Toast.makeText(this, "登録失敗", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
