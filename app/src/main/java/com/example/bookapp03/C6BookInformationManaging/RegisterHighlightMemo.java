package com.example.bookapp03.C6BookInformationManaging;

import android.content.Context;

import com.example.bookapp03.data.model.HighlightMemoData;
import com.example.bookapp03.C6BookInformationManaging.database.BookInformationDatabase;
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoDao;
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * モジュール名: ハイライト・メモ登録情報保存
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ユーザ／書籍ごとのハイライトメモをローカルDBに保存するクラス
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
public class RegisterHighlightMemo {

    private final HighlightMemoDao dao;
    private final ExecutorService executor;

    /**
     * コンストラクタ
     *
     * @param context アプリケーションコンテキスト
     */
    public RegisterHighlightMemo(Context context) {
        BookInformationDatabase db = BookInformationDatabase.getDatabase(context);
        this.dao = db.highlightMemoDao();
        this.executor = Executors.newFixedThreadPool(2);
    }

    /**
     * ハイライトメモをローカルDBに登録する。
     * overallSummaryおよびisPublicはデフォルト値を使用する。
     *
     * @param uid      ユーザUID
     * @param volumeId 書籍ボリュームID
     * @param data     ハイライトメモデータ（ページ、行、メモ）
     * @return true=登録成功、false=失敗
     */
    public boolean registerHighlightMemo(
            String uid,
            String volumeId,
            HighlightMemoData data
    ) {
        try {
            Future<Boolean> future = executor.submit(() -> {
                HighlightMemoEntity entity = new HighlightMemoEntity(
                        uid,
                        volumeId,
                        data.getPage(),
                        data.getLine(),
                        data.getMemoContent()
                );
                long result = dao.insert(entity);
                return result > 0;
            });
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
