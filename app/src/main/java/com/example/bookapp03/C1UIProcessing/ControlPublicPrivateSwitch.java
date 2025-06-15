package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
import android.widget.Switch;

import com.example.bookapp03.C6BookInformationManaging.database.SummaryDao;
import com.example.bookapp03.C6BookInformationManaging.database.SummaryEntity;

import java.util.concurrent.ExecutorService;

/**
 * モジュール名: 公開・非公開スイッチ制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: 全体まとめの公開・非公開フラグをDBから読み込み、
 *       スイッチに反映するクラス
 * 履歴:
 *   2025/06/15 鶴田凌 新規作成
 */
public class ControlPublicPrivateSwitch {
    private final Activity     activity;
    private final SummaryDao   summaryDao;
    private final String       uid;
    private final String       volumeId;
    private final ExecutorService executor;

    /**
     * @param activity   呼び出し元 Activity
     * @param summaryDao 要約用 DAO
     * @param uid        ユーザID
     * @param volumeId   書籍ボリュームID
     * @param executor   DB操作実行用スレッドプール
     */
    public ControlPublicPrivateSwitch(
        Activity activity,
        SummaryDao summaryDao,
        String uid,
        String volumeId,
        ExecutorService executor
    ) {
        this.activity   = activity;
        this.summaryDao = summaryDao;
        this.uid        = uid;
        this.volumeId   = volumeId;
        this.executor   = executor;
    }

    /**
     * スイッチをバインドし、DBから要約エンティティを取得して
     * isPublic を反映する。
     *
     * @param sw 公開・非公開スイッチ
     */
    public void bind(Switch sw) {
        executor.execute(() -> {
            SummaryEntity summary = summaryDao.getSummary(uid, volumeId);
            if (summary != null) {
                boolean isPub = summary.isPublic;
                activity.runOnUiThread(() -> sw.setChecked(isPub));
            }
        });
    }
}