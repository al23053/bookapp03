package com.example.bookapp03.C1UIProcessing;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp03.R;
import com.example.bookapp03.C2UserInformationProcessing.UserInfo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * モジュール名: アカウント切替画面表示
 * 作成者: 鶴田凌
 * 作成日: 2025/06/16
 * 概要: Firestore 上の登録済みユーザを読み込み、RadioGroup で選択→切替処理を実行する画面
 * 履歴:
 * 2025/06/16 鶴田凌 新規作成
 */
public class DisplayAccountSwitching extends AppCompatActivity {
    private ImageButton btnBack;
    private RadioGroup radioGroup;
    private ControlAccountSwitching ctrlSwitch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accountswitchingdisplay);

        btnBack = findViewById(R.id.btnBack);
        radioGroup = findViewById(R.id.radioAccountGroup);

        // １) 戻るボタン
        btnBack.setOnClickListener(v ->
                new ControlButtonToSetting().setToSetting(DisplayAccountSwitching.this)
        );

        // ２) Firestore から user 情報取得 → RadioButton 生成
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnSuccessListener(snap -> {
                    for (QueryDocumentSnapshot doc : snap) {
                        String uid = doc.getId();
                        String nick = doc.getString("nickname");
                        String mail = doc.getString("email");
                        if (nick == null || mail == null) continue;
                        RadioButton rb = new RadioButton(this);
                        rb.setText(nick + " (" + mail + ")");
                        // ユーザ情報をタグにくくりつけ
                        rb.setTag(new UserInfo(uid, nick, mail));
                        radioGroup.addView(rb);
                    }
                    // ３) 選択検知→ControlAccountSwitching に委譲
                    ctrlSwitch = new ControlAccountSwitching(this, radioGroup);
                    ctrlSwitch.bind();
                })
                .addOnFailureListener(e -> {
                    // 必要ならエラー処理
                });
    }
}
