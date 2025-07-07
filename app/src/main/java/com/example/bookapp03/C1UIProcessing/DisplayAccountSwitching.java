package com.example.bookapp03.C1UIProcessing;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bookapp03.C2UserInformationProcessing.LocalAccountStore;
import com.example.bookapp03.C2UserInformationProcessing.UserInfo;
import com.example.bookapp03.C5UserInformationManaging.UserAuthManager;
import com.example.bookapp03.R;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

/**
 * モジュール名: アカウント切替画面表示
 * 作成者: 鶴田凌
 * 作成日: 2025/06/16
 * 概要: SharedPreferences に保持した UID リストを元に、
 *       Firestore から該当ユーザ情報を取得し RadioButton で表示する Activity
 * 履歴:
 *   2025/06/16 鶴田凌 新規作成
 *   2025/07/06 鶴田凌 UIDリスト個別取得に変更
 */
public class DisplayAccountSwitching extends AppCompatActivity {
    private RadioGroup radioGroup;
    private ImageButton btnBack;
    private String currentUid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accountswitchingdisplay);

        currentUid = UserAuthManager.getCurrentUid();
        btnBack = findViewById(R.id.btnBack);
        radioGroup = findViewById(R.id.radioAccountGroup);
        btnBack.setOnClickListener(v ->
            new ControlButtonToSetting().setToSetting(this)
        );

        List<String> localUids = LocalAccountStore.loadUids(this);
        if (localUids.isEmpty()) {
            localUids = List.of(currentUid);
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .whereIn(FieldPath.documentId(), localUids)
            .get()
            .addOnSuccessListener((QuerySnapshot snap) -> {
                for (var doc : snap.getDocuments()) {
                    String uid = doc.getId();
                    
                    String nick = doc.getString("nickname");
                   
                    String mail = doc.getString("email");
                    
                    Log.d("AcctSwitch", "Processing user: uid=" + uid + ", nick=" + nick + ", mail=" + mail);
                    
                    if (nick != null && mail != null) {
                        RadioButton rb = new RadioButton(this);
                        rb.setText(nick + " (" + mail + ")");
                        rb.setTag(new UserInfo(uid, nick, mail));
                        
                        // 直接色リソースを使用
                        rb.setTextColor(ContextCompat.getColor(this, android.R.color.primary_text_light));
                        
                        // パディングを追加してレイアウトを調整
                        rb.setPadding(16, 16, 16, 16);
                        
                        radioGroup.addView(rb);
                        
                        if (uid.equals(currentUid)) {
                            rb.setChecked(true);
                        }
                    }
                }
                Log.d("AcctSwitch", "Added RadioButtons: count=" + radioGroup.getChildCount());
                new ControlAccountSwitching(this, radioGroup).bind();
            })
            .addOnFailureListener(e ->
                Log.e("AcctSwitch", "Firestore error", e)
            );
    }
}
