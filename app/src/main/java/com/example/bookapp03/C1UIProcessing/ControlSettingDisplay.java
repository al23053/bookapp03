package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;
import android.util.Log;

import com.example.bookapp03.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * モジュール名: ユーザ設定画面UI制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/16
 * 概要: 設定項目のタップを監視し、該当画面へ遷移またはログアウト処理を呼び出す
 * 履歴:
 * 2025/06/16 鶴田凌 新規作成
 * 2025/07/01 増田学斗　.putExtra追加
 * 2025/07/07 増田学斗　AccountSettingActivityにnicknameとiconBase64を渡す処理を追加
 */
public class ControlSettingDisplay {
    private final Activity activity;

    /**
     * コンストラクタ
     *
     * @param activity 呼び出し元の Activity
     */
    public ControlSettingDisplay(Activity activity) {
        this.activity = activity;
    }

    /**
     * 各設定項目にリスナーを設定する。
     *
     * @param btnNicknameIcon  ニックネーム・アイコン設定画面へ遷移する TextView
     * @param btnGenre         好きな本のジャンル設定画面へ遷移する62 TextView
     * @param btnDarkMode      ダークモード切替画面へ遷移する TextView
     * @param btnAccountSwitch アカウント切替画面へ遷移する TextView
     * @param btnLogout        ログアウト確認を行う TextView
     */
    public void bind(TextView btnNicknameIcon, TextView btnGenre, TextView btnDarkMode, TextView btnAccountSwitch, TextView btnLogout) {
        btnNicknameIcon.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                // ログインしていない場合は空データを送って起動
                Intent intent = new Intent(activity, AccountSettingActivity.class);
                intent.putExtra("isFirstTime", false);
                activity.startActivity(intent);
                return;
            }

            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // FirestoreからnicknameとiconBase64を取得してIntentに渡す
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                String nickname = doc.getString("nickname");
                String iconBase64 = doc.getString("iconBase64");

                Intent intent = new Intent(activity, AccountSettingActivity.class);
                intent.putExtra("nickname", nickname);
                intent.putExtra("iconBase64", iconBase64);
                intent.putExtra("isFirstTime", false);
                activity.startActivity(intent);
            }).addOnFailureListener(e -> {
                Log.e("ControlSetting", "Firestore取得失敗", e);
                // 失敗してもとりあえず画面を開く
                Intent intent = new Intent(activity, AccountSettingActivity.class);
                intent.putExtra("isFirstTime", false);
                activity.startActivity(intent);
            });
        });

        btnGenre.setOnClickListener(v -> {
            activity.startActivity(new Intent(activity, GenreSelectionActivity.class).putExtra("isFirstTime", false));
        });
        btnDarkMode.setOnClickListener(v -> activity.startActivity(new Intent(activity, DisplayDarkmodeSetting.class)));
        btnAccountSwitch.setOnClickListener(v -> activity.startActivity(new Intent(activity, DisplayAccountSwitching.class)));
        btnLogout.setOnClickListener(v -> new LogoutProcessing(activity).confirmLogout());
    }
}
