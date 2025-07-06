package com.example.bookapp03.C1UIProcessing;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp03.C2UserInformationProcessing.LocalAccountStore;  // ← 追加
import com.example.bookapp03.C2UserInformationProcessing.UserInfo;
import com.example.bookapp03.C5UserInformationManaging.UserAuthManager;
import com.example.bookapp03.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

/**
 * モジュール名: アカウント切替画面表示
 * 作成者: 鶴田凌
 * 作成日: 2025/06/16
 * 概要: 端末に保存された UID リストを元に、自分のアカウントだけを
 *       Firestore から取得・表示し、切替を行う Activity
 * 履歴:
 *   2025/06/16 鶴田凌 新規作成
 *   2025/07/06 鶴田凌 ローカル UID リストフィルタリング対応
 */
public class DisplayAccountSwitching extends AppCompatActivity {
    private ImageButton btnBack;
    private RadioGroup radioGroup;
    private ControlAccountSwitching ctrlSwitch;
    private String currentUid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accountswitchingdisplay);

        currentUid = UserAuthManager.getCurrentUid();
        btnBack = findViewById(R.id.btnBack);
        radioGroup = findViewById(R.id.radioAccountGroup);

        // 戻るボタン
        btnBack.setOnClickListener(v ->
            new ControlButtonToSetting().setToSetting(this)
        );

        // 端末に保存された UID リストを読み込む
        List<String> localUids = LocalAccountStore.loadUids(this);

        // Firestore から users コレクションを全件取得し、
        // ローカル UID リストに含まれるものだけを表示
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
          .get()
          .addOnSuccessListener(snapshot -> {
              for (QueryDocumentSnapshot doc : snapshot) {
                  String uid = doc.getId();
                  if (!localUids.contains(uid)) continue;  // ローカル登録分のみ
                  String nick = doc.getString("nickname");
                  String mail = doc.getString("email");
                  if (nick != null && mail != null) {
                      RadioButton rb = new RadioButton(this);
                      rb.setText(nick + " (" + mail + ")");
                      rb.setTag(new UserInfo(uid, nick, mail));
                      radioGroup.addView(rb);
                      if (uid.equals(currentUid)) {
                          rb.setChecked(true);
                      }
                  }
              }
              ctrlSwitch = new ControlAccountSwitching(this, radioGroup);
              ctrlSwitch.bind();
          })
          .addOnFailureListener(e -> {
              // 必要ならエラー処理
          });
    }
}
