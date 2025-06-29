package com.example.bookapp03.C1UIProcessing;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.bookapp03.C5UserInformationManaging.UserAuthManager;
import com.example.bookapp03.C6BookInformationManaging.database.SummaryDao;
import com.example.bookapp03.C6BookInformationManaging.database.BookInformationDatabase;
import com.example.bookapp03.R;

/**
 * モジュール名: 全体まとめ登録画面表示
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: 全体まとめ登録画面を表示するクラス
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
public class DisplaySummary extends AppCompatActivity {
    
    private static final String TAG = "DisplaySummary";
    
    private String currentUid;
    private String currentVolumeId;

    private EditText summaryInput;
    private Switch switchPublic;
    private ImageButton btnBack, btnMenu, btnRegisterSummary;
    
    // DrawerLayout の追加
    private DrawerLayout drawerLayout;

    private SummaryDao summaryDao;
    private ExecutorService executor;

    private ControlBackToHomeButton ctrlBack;
    private ControlHamburgerBar ctrlMenu;
    private ControlPublicPrivateSwitch ctrlSwitch;
    private ControlSummaryRegistrationButton ctrlRegister;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summarydisplay);
        
        Log.d(TAG, "DisplaySummary onCreate開始");

        try {
            // SummaryDao／Executor 初期化
            summaryDao = BookInformationDatabase
                    .getDatabase(this)
                    .summaryDao();
            executor = Executors.newSingleThreadExecutor();

            // View バインド
            initializeViews();

            // UID は FirebaseAuth、volumeId は Intent extras から取得
            currentUid = UserAuthManager.getCurrentUid();
            currentVolumeId = getIntent().getStringExtra("volumeId");
            
            Log.d(TAG, "UID: " + currentUid + ", VolumeID: " + currentVolumeId);

            // 各コントローラ生成・バインド
            setupControllers();
            
            Log.d(TAG, "DisplaySummary 初期化完了");
            
        } catch (Exception e) {
            Log.e(TAG, "DisplaySummary 初期化エラー", e);
        }
    }
    
    /**
     * View要素を初期化
     */
    private void initializeViews() {
        summaryInput = findViewById(R.id.summaryInput);
        switchPublic = findViewById(R.id.switchPublic);
        btnBack = findViewById(R.id.btnBack);
        btnMenu = findViewById(R.id.btnMenu);
        btnRegisterSummary = findViewById(R.id.btnRegisterSummary);
        
        // DrawerLayout の初期化は行わない（summarydisplay.xml には存在しないため）
        // ハンバーガーメニューは別画面遷移で処理
    }
    
    /**
     * コントローラを設定
     */
    private void setupControllers() {
        ctrlBack = new ControlBackToHomeButton(this);
        ctrlBack.bind(btnBack);

        ctrlMenu = new ControlHamburgerBar(this, currentUid, currentVolumeId);
        ctrlMenu.bind(btnMenu);

        ctrlSwitch = new ControlPublicPrivateSwitch(
                this, summaryDao, currentUid, currentVolumeId, executor
        );
        ctrlSwitch.bind(switchPublic);

        ctrlRegister = new ControlSummaryRegistrationButton(
                this, summaryDao, currentUid, currentVolumeId, executor
        );
        ctrlRegister.bind(btnRegisterSummary, summaryInput, switchPublic);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
