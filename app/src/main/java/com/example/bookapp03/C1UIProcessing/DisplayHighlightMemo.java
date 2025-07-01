package com.example.bookapp03.C1UIProcessing;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
 * 2025/07/01 鶴田凌 エラーハンドリングと表示処理を改善
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
        
        Log.d(TAG, "DisplayHighlightMemo onCreate開始");
        
        try {
            setContentView(R.layout.highlightmemodisplay);
            
            // Intent からパラメータ取得
            uid = getIntent().getStringExtra("uid");
            volumeId = getIntent().getStringExtra("volumeId");
            
            Log.d(TAG, "Intent データ - UID: " + uid + ", VolumeID: " + volumeId);
            
            // パラメータ検証
            if (uid == null || uid.isEmpty()) {
                Log.e(TAG, "UIDが null または空です");
                Toast.makeText(this, "ユーザー情報が無効です", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            if (volumeId == null || volumeId.isEmpty()) {
                Log.e(TAG, "VolumeIDが null または空です");
                Toast.makeText(this, "書籍情報が無効です", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            // Database 初期化
            highlightMemoDao = BookInformationDatabase
                    .getDatabase(this)
                    .highlightMemoDao();
            executor = Executors.newSingleThreadExecutor();
            
            // View 初期化
            initializeViews();
            
            // データ読み込み
            loadHighlightMemos();
            
            // ドロワーを自動で開く（少し遅延して実行）
            recyclerView.postDelayed(() -> {
                openDrawer();
            }, 300);
            
        } catch (Exception e) {
            Log.e(TAG, "DisplayHighlightMemo 初期化エラー", e);
            Toast.makeText(this, "画面の初期化に失敗しました", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    /**
     * View要素を初期化
     */
    private void initializeViews() {
        try {
            drawerLayout = findViewById(R.id.drawer_layout);
            recyclerView = findViewById(R.id.recyclerHighlight);
            
            if (drawerLayout == null) {
                Log.e(TAG, "DrawerLayout が見つかりません");
                throw new RuntimeException("DrawerLayout not found");
            }
            
            if (recyclerView == null) {
                Log.e(TAG, "RecyclerView が見つかりません");
                throw new RuntimeException("RecyclerView not found");
            }
            
            // RecyclerView設定
            adapter = new HighlightMemoAdapter();
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
            
            Log.d(TAG, "RecyclerView設定完了");
            
            // DrawerLayout のリスナー設定
            drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                    // 何もしない
                }

                @Override
                public void onDrawerOpened(@NonNull View drawerView) {
                    Log.d(TAG, "ドロワーが開かれました");
                }

                @Override
                public void onDrawerClosed(@NonNull View drawerView) {
                    Log.d(TAG, "ドロワーが閉じられました - 画面を終了");
                    // ドロワーが閉じられたら画面を終了して元の画面に戻る
                    finish();
                }

                @Override
                public void onDrawerStateChanged(int newState) {
                    // 何もしない
                }
            });
            
            Log.d(TAG, "View初期化完了");
            
        } catch (Exception e) {
            Log.e(TAG, "View初期化エラー", e);
            throw e;
        }
    }
    
    /**
     * ハイライトメモデータを読み込み
     */
    private void loadHighlightMemos() {
        Log.d(TAG, "ハイライトメモ読み込み開始");
        
        executor.execute(() -> {
            try {
                Log.d(TAG, "データベースクエリ実行 - UID: " + uid + ", VolumeID: " + volumeId);
                
                List<HighlightMemoEntity> entities = highlightMemoDao.getByUserAndVolume(uid, volumeId);
                
                Log.d(TAG, "クエリ結果: " + entities.size() + "件");
                
                List<HighlightMemoData> dataList = new ArrayList<>();
                
                for (HighlightMemoEntity entity : entities) {
                    HighlightMemoData data = new HighlightMemoData(
                            entity.page,
                            entity.line,
                            entity.memo
                    );
                    dataList.add(data);
                    Log.d(TAG, "メモ追加 - Page: " + entity.page + ", Line: " + entity.line + ", Memo: " + entity.memo);
                }
                
                runOnUiThread(() -> {
                    if (adapter != null) {
                        adapter.setItems(dataList);
                        Log.d(TAG, "ハイライトメモ " + dataList.size() + " 件を表示");
                        
                        if (dataList.isEmpty()) {
                            Toast.makeText(this, "この書籍にはハイライトメモがありません", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, dataList.size() + "件のメモを表示中", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Adapter が null です");
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "ハイライトメモ読み込みエラー", e);
                runOnUiThread(() -> {
                    if (adapter != null) {
                        adapter.setItems(new ArrayList<>());
                    }
                    Toast.makeText(this, "データの読み込みに失敗しました", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    /**
     * ドロワーを開く
     */
    private void openDrawer() {
        try {
            if (drawerLayout != null) {
                Log.d(TAG, "ドロワーを開きます");
                drawerLayout.openDrawer(GravityCompat.END);
            } else {
                Log.e(TAG, "DrawerLayout が null のためドロワーを開けません");
            }
        } catch (Exception e) {
            Log.e(TAG, "ドロワーを開く際にエラー", e);
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
        Log.d(TAG, "DisplayHighlightMemo onDestroy完了");
    }
}
