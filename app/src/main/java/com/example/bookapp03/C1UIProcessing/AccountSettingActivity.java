
/**
 * モジュール名: AccountSettingActivity
 * 作成者: 増田学斗
 * 作成日: 2025/06/15
 * 概要: ニックネームとアイコンの設定処理
 * 履歴:
 * 2025/06/15 増田学斗 新規作成
 */

package com.example.bookapp03.C1UIProcessing;

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
import com.example.bookapp03.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);

        imageViewIcon = findViewById(R.id.imageViewIcon);
        buttonChooseImage = findViewById(R.id.buttonChooseImage);
        editTextNickname = findViewById(R.id.editTextNickname);
        buttonNext = findViewById(R.id.buttonNext);

        buttonChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK);
        });

        buttonNext.setOnClickListener(v -> saveUserData());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageViewIcon.setImageURI(selectedImageUri);
        }
    }

    private void saveUserData() {
        String nickname = editTextNickname.getText().toString().trim();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "ユーザーがログインしていません", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        String iconUriStr = (selectedImageUri != null) ? selectedImageUri.toString() : "";

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("nickname", nickname);
        userMap.put("iconUri", iconUriStr);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener(unused -> {
                    Log.d("AccountSetting", "Firestore保存成功、遷移開始");
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(AccountSettingActivity.this, GenreSelectionActivity.class);
                    intent.putExtra("nickname", nickname);
                    intent.putExtra("iconUri", iconUriStr);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "保存失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("AccountSetting", "Firestore保存失敗", e);
                });
    }
}
