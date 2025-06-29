package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.concurrent.ExecutorService;

import com.example.bookapp03.C3BookInformationProcessing.TransmitSummary;
import com.example.bookapp03.C6BookInformationManaging.database.SummaryDao;
import com.example.bookapp03.R;

/**
 * モジュール名: 全体まとめ登録ボタン制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: 全体まとめ登録ボタン押下時に文字数チェックと登録処理を行うクラス
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
public class ControlSummaryRegistrationButton {
    private final Activity activity;
    private final SummaryDao dao;
    private final String uid;
    private final String volumeId;
    private final ExecutorService executor;

    /**
     * コンストラクタ
     *
     * @param activity 呼び出し元 Activity
     * @param dao      要約用ローカルDBアクセス DAO (SummaryDao)
     * @param uid      ユーザID
     * @param volumeId 書籍ボリュームID
     * @param executor DB操作実行用スレッドプール
     */
    public ControlSummaryRegistrationButton(
            Activity activity,
            SummaryDao dao,
            String uid,
            String volumeId,
            ExecutorService executor
    ) {
        this.activity = activity;
        this.dao = dao;
        this.uid = uid;
        this.volumeId = volumeId;
        this.executor = executor;
    }

    /**
     * ボタンと入力欄・スイッチをバインドし、押下時に500文字制限チェック後、
     * TransmitSummary を呼び登録・トースト表示・Activity終了を行う。
     *
     * @param btnRegisterSummary 全体まとめ登録ボタン
     * @param summaryInput       要約入力用 EditText
     * @param switchPublic       公開/非公開スイッチ
     */
    public void bind(
            ImageButton btnRegisterSummary,
            EditText summaryInput,
            Switch switchPublic
    ) {
        btnRegisterSummary.setOnClickListener(v -> {
            String overall = summaryInput.getText().toString();
            // 文字数超過チェック
            if (overall.length() > 500) {
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.error)
                        .setMessage("全体まとめは500文字以内で入力してください。")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return;
            }
            // 登録処理（別スレッドで実行）
            executor.execute(() -> {
                boolean ok = new TransmitSummary(activity)
                        .transmitSummary(uid, volumeId, overall, switchPublic.isChecked());
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity,
                            ok ? "登録成功" : "登録失敗",
                            Toast.LENGTH_SHORT
                    ).show();
                    if (ok) activity.finish();
                });
            });
        });
    }
}
