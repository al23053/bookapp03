package com.example.bookapp03.C1UIProcessing;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.view.GravityCompat;

import com.example.bookapp03.R;
import com.example.bookapp03.C6BookInformationManaging.database.BookInformationDatabase;
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoDao;
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * モジュール名: ハイライトメモ表示画面
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ハイライトメモ一覧を表示する画面
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
public class DisplayHighlightMemo extends AppCompatActivity {
    
    private static final String TAG = "DisplayHighlightMemo";
    
    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private HighlightMemoAdapter adapter;
    
    private String uid;
    private String volumeId;
    
    private HighlightMemoDao highlightMemoDao;
    private ExecutorService executor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.highlightmemodisplay);
        
        Log.d(TAG, "DisplayHighlightMemo onCreate開始");
        
        try {
            // Intent からパラメータ取得
            uid = getIntent().getStringExtra("uid");
            volumeId = getIntent().getStringExtra("volumeId");
            
            Log.d(TAG, "UID: " + uid + ", VolumeID: " + volumeId);
            
            // Database 初期化
            highlightMemoDao = BookInformationDatabase
                    .getDatabase(this)
                    .highlightMemoDao();
            executor = Executors.newSingleThreadExecutor();
            
            // View 初期化
            initializeViews();
            
            // データ読み込み
            loadHighlightMemos();
            
            // ドロワーを自動で開く
            openDrawer();
            
        } catch (Exception e) {
            Log.e(TAG, "DisplayHighlightMemo 初期化エラー", e);
            finish();
        }
    }
    
    /**
     * View要素を初期化
     */
    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        recyclerView = findViewById(R.id.recyclerHighlight);
        
        // RecyclerView設定
        adapter = new HighlightMemoAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        // DrawerLayout のリスナー設定
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(androidx.annotation.NonNull android.view.View drawerView, float slideOffset) {
                // 何もしない
            }

            @Override
            public void onDrawerOpened(androidx.annotation.NonNull android.view.View drawerView) {
                Log.d(TAG, "ドロワーが開かれました");
            }

            @Override
            public void onDrawerClosed(androidx.annotation.NonNull android.view.View drawerView) {
                Log.d(TAG, "ドロワーが閉じられました - 画面を終了");
                // ドロワーが閉じられたら画面を終了して元の画面に戻る
                finish();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // 何もしない
            }
        });
    }
    
    /**
     * ハイライトメモデータを読み込み
     */
    private void loadHighlightMemos() {
        if (uid == null || volumeId == null) {
            Log.e(TAG, "UID または VolumeID が null です");
            return;
        }
        
        executor.execute(() -> {
            try {
                List<HighlightMemoEntity> entities = highlightMemoDao.getHighlightMemo(uid, volumeId);
                List<HighlightMemoData> dataList = new ArrayList<>();
                
                for (HighlightMemoEntity entity : entities) {
                    HighlightMemoData data = new HighlightMemoData(
                            entity.page,
                            entity.line,
                            entity.memo
                    );
                    dataList.add(data);
                }
                
                runOnUiThread(() -> {
                    adapter.setItems(dataList);
                    Log.d(TAG, "ハイライトメモ " + dataList.size() + " 件を表示");
                });
                
            } catch (Exception e) {
                Log.e(TAG, "ハイライトメモ読み込みエラー", e);
                runOnUiThread(() -> {
                    adapter.setItems(new ArrayList<>());
                });
            }
        });
    }
    
    /**
     * ドロワーを開く
     */
    private void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }
    
    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            // ドロワーが開いている場合は閉じる
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            // ドロワーが閉じている場合は通常のバック処理
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
