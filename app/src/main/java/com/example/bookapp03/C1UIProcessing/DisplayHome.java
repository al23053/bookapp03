package com.example.bookapp03.C1UIProcessing;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp03.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.bookapp03.C5UserInformationManaging.UserAuthManager;
import com.example.bookapp03.C6BookInformationManaging.VolumeIdProvider;

/**
 * モジュール名: ホーム画面表示
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ホーム画面を表示し、ハイライトメモ登録および画面遷移を制御するクラス
 * 履歴:
 * 　2025/06/15 鶴田凌 新規作成
 */

public class DisplayHome extends AppCompatActivity {

    /**
     * ページ数入力欄
     */
    private EditText editPage;

    /**
     * 行数入力欄
     */
    private EditText editLine;

    /**
     * メモ入力欄
     */
    private EditText editMemo;

    /**
     * ハイライト・メモを登録する ImageButton
     */
    private ImageButton btnHighlightSubmit;

    /**
     * 全体まとめ画面へ遷移するボタン
     */
    private Button btnSummary;

    /**
     * ユーザ設定画面へ遷移する ImageButton
     */
    private ImageButton btnUserSettings;

    /**
     * ハイライトメモの入力チェックと DB 登録を行うコントローラ
     */
    private ControlHighlightMemo controlHighlightMemo;

    /**
     * 全体まとめ画面遷移を制御するコントローラ
     */
    private ControlButtonToSummary controlButtonToSummary;

    /**
     * ユーザ設定画面遷移を制御するコントローラ
     */
    private ControlButtonToSetting controlButtonToSetting;

    /**
     * ホーム画面を表示し、各 UI 部品のバインドとコントローラの初期化・リスナー設定を行う
     *
     * @param savedInstanceState アクティビティ状態の保存情報
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homedisplay);

        // 1) View バインド
        editPage = findViewById(R.id.editPage);
        editLine = findViewById(R.id.editLine);
        editMemo = findViewById(R.id.editMemo);
        btnHighlightSubmit = findViewById(R.id.btnRegister);
        btnSummary = findViewById(R.id.registerButton);
        btnUserSettings = findViewById(R.id.btnUserSettings);

        // 2) UID および volumeId の取得とコントローラ初期化
        String uid = UserAuthManager.getCurrentUid();
        String volumeId = getIntent().getStringExtra("volumeId");
        if (volumeId == null) volumeId = "";
        controlHighlightMemo = new ControlHighlightMemo(this, uid, volumeId);
        controlButtonToSummary = new ControlButtonToSummary();
        controlButtonToSetting = new ControlButtonToSetting();

        // 3) ハイライト・メモ登録処理
        btnHighlightSubmit.setOnClickListener(v -> {
            try {
                int page = Integer.parseInt(editPage.getText().toString());
                int line = Integer.parseInt(editLine.getText().toString());
                String memo = editMemo.getText().toString();
                HighlightMemoData data = controlHighlightMemo.getHighlightMemo(page, line, memo);
                boolean ok = controlHighlightMemo.sendData(data);
                Toast.makeText(this, ok ? "登録成功" : "登録失敗", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "ページ・行数は数字で入力してください", Toast.LENGTH_SHORT).show();
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // 4) 全体まとめ画面へ遷移
        btnSummary.setOnClickListener(v -> controlButtonToSummary.setToSummary(this));

        // 5) ユーザ設定画面へ遷移
        btnUserSettings.setOnClickListener(v -> controlButtonToSetting.setToSetting(this));

        // 6) ナビゲーションバーアイテム選択
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setOnItemSelectedListener(item -> {
            new ControlBottomNavigationBar().handledisplay(item.getItemId(), this);
            return true;
        });
    }
}
