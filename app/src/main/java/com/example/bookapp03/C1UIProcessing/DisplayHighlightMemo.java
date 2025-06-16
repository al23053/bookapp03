package com.example.bookapp03.C1UIProcessing;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.bookapp03.C6BookInformationManaging.database.BookInformationDatabase;
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoDao;
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoEntity;
import com.example.bookapp03.R;

/**
 * モジュール名: ハイライトメモ表示
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ローカルDBから読み込んだハイライトメモを右側からスライドするナビゲーションドロワーに一覧表示するActivity
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
public class DisplayHighlightMemo extends AppCompatActivity {

    /**
     * DrawerLayout 本体（右側スライドドロワー）
     */
    private DrawerLayout drawer;

    /**
     * 表示用 RecyclerView
     */
    private RecyclerView recycler;

    /**
     * RecyclerView に渡すアダプター
     */
    private HighlightMemoAdapter adapter;

    /**
     * Room DAO：ハイライトメモアクセス
     */
    private HighlightMemoDao dao;

    /**
     * 現在のユーザID
     */
    private String uid;

    /**
     * 現在の書籍ボリュームID
     */
    private String volumeId;

    /**
     * DB操作用スレッドプール
     */
    private ExecutorService exec;

    /**
     * Activity 起動時にレイアウトをセットし、Intent extras・DAO準備、
     * DBからの読み込みとドロワー表示を行う。
     *
     * @param savedInstanceState 前回の状態保存情報
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.highlightmemodisplay);

        drawer = findViewById(R.id.drawer_layout);
        recycler = findViewById(R.id.recyclerHighlight);
        adapter = new HighlightMemoAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        // IntentからUID/volumeIdを取得
        uid = getIntent().getStringExtra("uid");
        volumeId = getIntent().getStringExtra("volumeId");

        dao = BookInformationDatabase.getDatabase(this).highlightMemoDao();
        exec = Executors.newSingleThreadExecutor();

        // DBから読み込み、ドロワーを開いて表示
        exec.execute(() -> {
            List<HighlightMemoEntity> list = dao.getByUserAndVolume(uid, volumeId);
            List<HighlightMemoData> data = new ArrayList<>();
            for (HighlightMemoEntity e : list) {
                data.add(new HighlightMemoData(e.page, e.line, e.memo));
            }
            runOnUiThread(() -> {
                adapter.setItems(data);
                drawer.openDrawer(androidx.core.view.GravityCompat.END);
            });
        });
    }
}
