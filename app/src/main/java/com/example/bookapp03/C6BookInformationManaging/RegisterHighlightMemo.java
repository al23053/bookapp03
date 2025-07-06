package com.example.bookapp03.C6BookInformationManaging;

import android.content.Context;
import android.util.Log;
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

    private static final String TAG = "RegisterHighlightMemo";
    
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
        this.context = context.getApplicationContext();
    }

    /**
     * ハイライトメモを登録する
     *
     * @param uid ユーザID
     * @param volumeId 書籍ボリュームID
     * @param data ハイライトメモデータ
     * @return 登録成功時 true、失敗時 false
     */
    public boolean registerHighlightMemo(String uid, String volumeId, HighlightMemoData data) {
        ExecutorService executor = null;
        try {
            Log.d(TAG, "=== データベース登録開始 ===");
            Log.d(TAG, "UID: " + uid);
            Log.d(TAG, "VolumeID: " + volumeId);
            Log.d(TAG, "Page: " + data.getPage());
            Log.d(TAG, "Line: " + data.getLine());
            Log.d(TAG, "Memo: " + data.getMemo());
            
            executor = Executors.newSingleThreadExecutor();
            
            Future<Boolean> future = executor.submit(() -> {
                try {
                    Log.d(TAG, "データベース取得中...");
                    HighlightMemoDao dao = BookInformationDatabase
                            .getDatabase(context)
                            .highlightMemoDao();
                    Log.d(TAG, "DAO取得完了");

                    HighlightMemoEntity entity = new HighlightMemoEntity(
                            uid,
                            volumeId,
                            data.getPage(),
                            data.getLine(),
                            data.getMemo()
                    );
                    Log.d(TAG, "エンティティ作成完了");

                    // データベースにハイライトメモを挿入
                    long result = dao.insert(entity);
                    Log.d(TAG, "データベース挿入結果: " + result);
                    
                    boolean success = result > 0;
                    Log.d(TAG, "登録成功: " + success);
                    return success;
                    
                } catch (Exception e) {
                    Log.e(TAG, "データベース操作エラー", e);
                    return false;
                }
            });

            boolean result = future.get();
            Log.d(TAG, "最終登録結果: " + result);
            return result;

        } catch (Exception e) {
            Log.e(TAG, "ハイライトメモ登録エラー", e);
            e.printStackTrace();
            return false;
        } finally {
            if (executor != null) {
                executor.shutdown();
            }
        }
    }
}
