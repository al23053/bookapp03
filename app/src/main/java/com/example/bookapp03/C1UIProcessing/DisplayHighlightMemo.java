package com.example.bookapp03.C1UIProcessing;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
 * 2025/07/02 鶴田凌 通常画面として修正
 */
public class DisplayHighlightMemo extends AppCompatActivity {
    
    private static final String TAG = "DisplayHighlightMemo";
    
    private RecyclerView recyclerView;
    private TextView txtNoMemos;
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
            Log.d(TAG, "レイアウト設定完了");
            
            // Intent からパラメータ取得
            uid = getIntent().getStringExtra("uid");
            volumeId = getIntent().getStringExtra("volumeId");
            
            Log.d(TAG, "受信したUID: " + uid);
            Log.d(TAG, "受信したVolumeID: " + volumeId);
            
            // パラメータ検証
            if (uid == null || uid.trim().isEmpty()) {
                Log.e(TAG, "UIDが無効です");
                showErrorAndFinish("ユーザー情報が無効です");
                return;
            }
            
            if (volumeId == null || volumeId.trim().isEmpty()) {
                Log.e(TAG, "VolumeIDが無効です");
                showErrorAndFinish("書籍情報が無効です");
                return;
            }
            
            // Database 初期化
            highlightMemoDao = BookInformationDatabase.getDatabase(this).highlightMemoDao();
            executor = Executors.newSingleThreadExecutor();
            Log.d(TAG, "Database初期化完了");
            
            // View 初期化
            initializeViews();
            
            // データ読み込み
            loadHighlightMemos();
            
            Log.d(TAG, "DisplayHighlightMemo 初期化完了");
            
        } catch (Exception e) {
            Log.e(TAG, "DisplayHighlightMemo 初期化エラー", e);
            showErrorAndFinish("画面の初期化に失敗しました: " + e.getMessage());
        }
    }
    
    private void initializeViews() {
        // 戻るボタン
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        
        // RecyclerView
        recyclerView = findViewById(R.id.recyclerHighlight);
        txtNoMemos = findViewById(R.id.txtNoMemos);
        
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerView が見つかりません");
            throw new RuntimeException("RecyclerView not found");
        }
        
        // Adapter設定
        adapter = new HighlightMemoAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        Log.d(TAG, "View初期化完了");
    }
    
    private void loadHighlightMemos() {
        Log.d(TAG, "ハイライトメモ読み込み開始");
        
        executor.execute(() -> {
            try {
                List<HighlightMemoEntity> entities = highlightMemoDao.getByUserAndVolume(uid, volumeId);
                Log.d(TAG, "データベースクエリ結果: " + entities.size() + "件");
                
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
                    updateUI(dataList);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "ハイライトメモ読み込みエラー", e);
                runOnUiThread(() -> {
                    updateUI(new ArrayList<>());
                    Toast.makeText(this, "データの読み込みに失敗しました", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void updateUI(List<HighlightMemoData> dataList) {
        try {
            if (adapter != null) {
                adapter.setItems(dataList);
                Log.d(TAG, "ハイライトメモ " + dataList.size() + " 件を表示");
            }
            
            // メッセージ表示の制御
            if (txtNoMemos != null) {
                if (dataList.isEmpty()) {
                    txtNoMemos.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    txtNoMemos.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
            
            String message = dataList.isEmpty() ? 
                "この書籍にはハイライトメモがありません" : 
                dataList.size() + "件のメモを表示中";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e(TAG, "UI更新エラー", e);
        }
    }
    
    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
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
