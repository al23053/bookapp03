package com.example.bookapp03.C6BookInformationManaging;

import android.content.Context;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.example.bookapp03.C6BookInformationManaging.database.BookInformationDatabase;
import com.example.bookapp03.C6BookInformationManaging.database.SummaryDao;
import com.example.bookapp03.C6BookInformationManaging.database.SummaryEntity;

/**
 * モジュール名: 全体まとめ登録
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ユーザ／書籍ごとの全体まとめ情報をローカルDBとFirestoreに保存するクラス
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
public class RegisterSummary {

    private final SummaryDao dao;
    private final ExecutorService executor;
    private final FirebaseFirestore firestore;

    /**
     * コンストラクタ
     *
     * @param context アプリケーションコンテキスト
     */
    public RegisterSummary(Context context) {
        BookInformationDatabase db = BookInformationDatabase.getDatabase(context);
        this.dao = db.summaryDao();
        this.executor = Executors.newSingleThreadExecutor();
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * 全体まとめ情報を登録または更新する。
     * isPublicがtrueの場合はFirestoreにも保存する。
     *
     * @param uid            ユーザUID
     * @param volumeId       書籍ボリュームID
     * @param overallSummary 要約テキスト
     * @param isPublic       公開フラグ
     * @return true=ローカルDBへの登録・更新成功、false=失敗
     */
    public boolean registerSummary(
            String uid,
            String volumeId,
            String overallSummary,
            boolean isPublic
    ) {
        try {
            SummaryEntity entity = new SummaryEntity(uid, volumeId, overallSummary, isPublic);
            Future<Long> f = executor.submit(() -> dao.insert(entity));
            boolean localOk = f.get() > 0;

            if (isPublic && localOk) {
                Map<String, Object> data = new HashMap<>();
                data.put("uid", uid);
                data.put("volumeId", volumeId);
                data.put("overallSummary", overallSummary);
                data.put("isPublic", true);
                firestore.collection("summaries")
                        .document(uid + "_" + volumeId)
                        .set(data);
            }
            return localOk;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
