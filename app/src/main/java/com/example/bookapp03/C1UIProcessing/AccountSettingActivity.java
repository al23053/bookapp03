/**
 * モジュール名: AccountSettingActivity
 * 作成者: 増田学斗
 * 作成日: 2025/06/15
 * 概要: ニックネームとアイコンの設定処理（Base64形式で画像保存、ログアウト状態でも表示対応）
 * 履歴:
 * 2025/06/15 増田学斗 新規作成
 * 2025/07/07 増田学斗 アイコン画像をBase64形式でFirestoreに保存するよう改修
 * 2025/07/07 増田学斗 Intent経由でnickname/iconBase64を受け取り、ログアウト状態でも表示対応
 */

package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.bookapp03.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AccountSettingActivity extends Activity {

    private boolean isFirstTime = true;
    private static final int REQUEST_CODE_IMAGE_PICK = 1;

    private EditText editTextNickname;
    private ImageView imageViewIcon;
    private Button buttonChooseImage;
    private Button buttonNext;

    private Uri selectedImageUri = null;
    private String iconBase64 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);

        isFirstTime = getIntent().getBooleanExtra("isFirstTime", true);

        imageViewIcon = findViewById(R.id.imageViewIcon);
        buttonChooseImage = findViewById(R.id.buttonChooseImage);
        editTextNickname = findViewById(R.id.editTextNickname);
        buttonNext = findViewById(R.id.buttonNext);

        buttonChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK);
        });

        buttonNext.setOnClickListener(v -> saveUserData());

        loadExistingUserData();
    }

    /**
     * Firestore または Intent から既存のニックネームと画像を読み込む
     */
    private void loadExistingUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = (user != null) ? user.getUid() : null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (uid != null) {
            // ログインしている場合は Firestore から取得
            db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String nickname = documentSnapshot.getString("nickname");
                    String iconBase64Str = documentSnapshot.getString("iconBase64");

                    if (nickname != null) editTextNickname.setText(nickname);
                    if (iconBase64Str != null && !iconBase64Str.isEmpty()) {
                        iconBase64 = iconBase64Str;
                        showImageFromBase64(iconBase64Str);
                    }
                }
            }).addOnFailureListener(e -> {
                Log.e("AccountSetting", "Firestore読み取り失敗", e);
            });
        } else {
            // ログアウト時は Intent から取得（例：前の画面から受け取った場合）
            String nickname = getIntent().getStringExtra("nickname");
            String iconBase64Str = getIntent().getStringExtra("iconBase64");

            if (nickname != null) editTextNickname.setText(nickname);
            if (iconBase64Str != null && !iconBase64Str.isEmpty()) {
                iconBase64 = iconBase64Str;
                showImageFromBase64(iconBase64Str);
            }
        }
    }

    /**
     * Base64文字列を画像に変換してImageViewに表示
     */
    private void showImageFromBase64(String base64) {
        try {
            byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            imageViewIcon.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e("AccountSetting", "画像表示失敗", e);
        }
    }

    /**
     * ギャラリーから画像を取得した後の処理
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageViewIcon.setImageURI(selectedImageUri);  // 仮表示
            iconBase64 = convertImageToBase64(selectedImageUri);  // Base64エンコード
        }
    }

    /**
     * ユーザー情報をFirestoreに保存する
     */
    private void saveUserData() {
        String nickname = editTextNickname.getText().toString().trim();

        if (nickname.isEmpty()) {
            Toast.makeText(this, "ニックネームを入力してください", Toast.LENGTH_SHORT).show();
            return;
        }

        if (iconBase64 == null) {
            Toast.makeText(this, "アイコン画像を選択してください", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "ユーザーがログインしていません", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("nickname", nickname);
        userMap.put("iconBase64", iconBase64);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).update(userMap).addOnSuccessListener(unused -> {
            if (isFirstTime) {
                Intent intent = new Intent(this, GenreSelectionActivity.class);
                intent.putExtra("nickname", nickname);
                intent.putExtra("iconBase64", iconBase64);
                intent.putExtra("isFirstTime", true);
                startActivity(intent);
                finish();
            } else {
                setResult(Activity.RESULT_OK);
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "保存に失敗しました", Toast.LENGTH_SHORT).show();
            Log.e("AccountSetting", "保存エラー", e);
        });
    }

    /**
     * URIから画像を読み込み、Base64文字列に変換する
     */
    private String convertImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            inputStream.close();

            byte[] imageBytes = byteBuffer.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e("AccountSetting", "画像Base64変換失敗", e);
            return null;
        }
    }
}
