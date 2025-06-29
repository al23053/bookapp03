package com.example.bookapp03.C6BookInformationManaging;

import android.content.Context;
import com.example.bookapp03.C6BookInformationManaging.database.BookInformationDatabase;
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoDao;
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoEntity;
import com.example.bookapp03.C1UIProcessing.HighlightMemoData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * モジュール名: ハイライトメモ登録処理
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ハイライトメモをデータベースに登録する処理
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
public class RegisterHighlightMemo {

    /**
     * Android コンテキスト
     */
    private Context context;

    /**
     * コンストラクタ
     *
     * @param context Android コンテキスト
     */
    public RegisterHighlightMemo(Context context) {
        this.context = context;
    }

    /**
     * ハイライトメモを登録する
     *
     * @param uid ユーザID
     * @param volumeId 書籍ボリュームID
     * @param data ハイライトメモデータ
     * @return 登録成功時 true、失敗時 false
     */
    public boolean registerHighlightMemo(
            String uid,
            String volumeId,
            HighlightMemoData data
    ) {
        ExecutorService executor = null;
        try {
            executor = Executors.newSingleThreadExecutor();
            
            Future<Boolean> future = executor.submit(() -> {
                HighlightMemoDao dao = BookInformationDatabase
                        .getDatabase(context)
                        .highlightMemoDao();

                HighlightMemoEntity entity = new HighlightMemoEntity(
                        uid,
                        volumeId,
                        data.getPage(),
                        data.getLine(),
                        data.getMemo()
                );

                // データベースにハイライトメモを挿入
                long result = dao.insert(entity);
                return result > 0; // 挿入成功時は行IDが返される
            });

            return future.get();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (executor != null) {
                executor.shutdown();
            }
        }
    }
}
