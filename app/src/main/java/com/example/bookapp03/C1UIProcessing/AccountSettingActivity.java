/**
 * モジュール名: AccountSettingActivity
 * 作成者: 増田学斗
 * 作成日: 2025/06/15
 * 概要: ニックネームとアイコン画像（Base64形式）の設定を行う Activity。
 * Firestore への保存や、ログアウト状態でも Intent 経由で表示できる構成。
 * 履歴:
 * 2025/06/15 増田学斗 新規作成
 * 2025/07/07 増田学斗 Base64形式でアイコンを保存するよう改修
 * 2025/07/07 増田学斗 ログアウト状態でのデータ表示対応（Intent経由）
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

    /**
     * 初回設定かどうかを示すフラグ（trueなら次画面へ遷移）
     */
    private boolean isFirstTime = true;

    /**
     * ギャラリー画像選択のリクエストコード
     */
    private static final int REQUEST_CODE_IMAGE_PICK = 1;

    /**
     * 入力欄：ニックネーム
     */
    private EditText editTextNickname;

    /**
     * 表示欄：選択されたアイコン画像
     */
    private ImageView imageViewIcon;

    /**
     * ボタン：画像選択
     */
    private Button buttonChooseImage;

    /**
     * ボタン：「次へ」
     */
    private Button buttonNext;

    /**
     * 選択された画像のURI
     */
    private Uri selectedImageUri = null;

    /**
     * Base64形式の画像データ
     */
    private String iconBase64 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);

        Log.d("AccountSetting", "onCreate開始");

        // 初回設定かどうかをIntentから取得
        isFirstTime = getIntent().getBooleanExtra("isFirstTime", true);

        // View 初期化
        initializeViews();

        // イベント設定
        setupButtonListeners();

        // FirestoreまたはIntentからデータを復元
        loadExistingUserData();
    }

    /**
     * View要素をバインド
     */
    private void initializeViews() {
        imageViewIcon = findViewById(R.id.imageViewIcon);
        buttonChooseImage = findViewById(R.id.buttonChooseImage);
        editTextNickname = findViewById(R.id.editTextNickname);
        buttonNext = findViewById(R.id.buttonNext);
    }

    /**
     * ボタン押下時の処理を設定
     */
    private void setupButtonListeners() {
        // ギャラリー画像選択
        buttonChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK);
        });

        // 次へボタン押下時、保存処理へ
        buttonNext.setOnClickListener(v -> saveUserData());
    }

    /**
     * Firestore または Intent からユーザーデータを読み込んで反映
     */
    private void loadExistingUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = (user != null) ? user.getUid() : null;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (uid != null) {
            // ログイン状態 → Firestoreから取得
            db.collection("users").document(uid).get().addOnSuccessListener(document -> {
                if (document.exists()) {
                    String nickname = document.getString("nickname");
                    String iconBase64Str = document.getString("iconBase64");

                    if (nickname != null) editTextNickname.setText(nickname);
                    if (iconBase64Str != null && !iconBase64Str.isEmpty()) {
                        iconBase64 = iconBase64Str;
                        showImageFromBase64(iconBase64Str);
                    }
                }
            }).addOnFailureListener(e -> Log.e("AccountSetting", "Firestore読み取り失敗", e));
        } else {
            // ログアウト状態 → Intentから受け取り
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
     * Base64文字列からBitmapに変換し、ImageViewに表示
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
     * ギャラリーから選択された画像をBase64に変換
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageViewIcon.setImageURI(selectedImageUri);  // 仮表示
            iconBase64 = convertImageToBase64(selectedImageUri);
        }
    }

    /**
     * 画像をBase64形式に変換して返す
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

    /**
     * 入力値のチェック・Firestoreへの保存・画面遷移
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
            Log.d("AccountSetting", "ユーザー情報をFirestoreに保存しました");

            if (isFirstTime) {
                // 初回設定ならジャンル選択画面へ遷移
                Intent intent = new Intent(this, GenreSelectionActivity.class);
                intent.putExtra("nickname", nickname);
                intent.putExtra("iconBase64", iconBase64);
                intent.putExtra("isFirstTime", true);
                startActivity(intent);
                finish();
            } else {
                // 再設定時は単に戻る
                setResult(Activity.RESULT_OK);
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "保存に失敗しました", Toast.LENGTH_SHORT).show();
            Log.e("AccountSetting", "Firestore保存エラー", e);
        });
    }
}
