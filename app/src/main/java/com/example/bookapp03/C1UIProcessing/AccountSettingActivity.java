/**
 * モジュール名: AccountSettingActivity
 * 作成者: 増田学斗
 * 作成日: 2025/06/15
 * 概要: ニックネームとアイコンの設定処理
 * 履歴:
 * 2025/06/15 増田学斗 新規作成
 */

package com.example.a1bapp.C1UIProcessing;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.a1bapp.GenreSelectionActivity;
import com.example.a1bapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class AccountSettingActivity extends Activity {

    /** ギャラリー画像選択のリクエストコード */
    private static final int REQUEST_CODE_IMAGE_PICK = 1;

    /** ユーザーが入力するニックネーム */
    private EditText editTextNickname;

    /** 選択したアイコン画像を表示するビュー */
    private ImageView imageViewIcon;

    /** ギャラリー画像を選択するボタン */
    private Button buttonChooseImage;

    /** 入力完了後、次の画面に進むボタン */
    private Button buttonNext;

    /** 選択された画像のURI */
    private Uri selectedImageUri = null;

    /**
     * アクティビティの初期化処理を行う。
     * @param savedInstanceState アクティビティの保存状態
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);

        imageViewIcon = findViewById(R.id.imageViewIcon);
        buttonChooseImage = findViewById(R.id.buttonChooseImage);
        editTextNickname = findViewById(R.id.editTextNickname);
        buttonNext = findViewById(R.id.buttonNext);

        // ギャラリーから画像を選択
        buttonChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK);
        });

        // 「次へ」ボタンが押されたとき、入力内容を保存して次画面へ遷移
        buttonNext.setOnClickListener(v -> saveUserData());
    }

    /**
     * ギャラリー選択後に呼び出され、画像を取得して表示する。
     * @param requestCode リクエストコード
     * @param resultCode 結果コード
     * @param data インテントデータ
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageViewIcon.setImageURI(selectedImageUri);
        }
    }

    /**
     * ユーザーのニックネームとアイコン画像URIをFirebaseに保存し、
     * ジャンル選択画面へ遷移する。
     */
    private void saveUserData() {
        String nickname = editTextNickname.getText().toString().trim();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "ユーザーがログインしていません", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        String iconUriStr = (selectedImageUri != null) ? selectedImageUri.toString() : "";

        // Firebase Realtime Database に保存するデータ
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("nickname", nickname);
        userMap.put("iconUri", iconUriStr); // Base64未使用構成

        // Firebaseにデータを保存し、成功時にジャンル選択画面へ遷移
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid);
        ref.setValue(userMap)
                .addOnSuccessListener(unused -> {
                    Log.d("AccountSetting", "Firebase保存成功、遷移開始");
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(AccountSettingActivity.this, GenreSelectionActivity.class);
                    intent.putExtra("nickname", nickname);
                    intent.putExtra("iconUri", iconUriStr);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "保存失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );

    }
}
