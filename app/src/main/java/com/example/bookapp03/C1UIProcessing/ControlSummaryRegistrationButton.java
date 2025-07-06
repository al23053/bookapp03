package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.bookapp03.C3BookInformationProcessing.TransmitSummary;
import com.example.bookapp03.C6BookInformationManaging.database.SummaryDao;
import com.example.bookapp03.R;

import java.util.concurrent.ExecutorService;

/**
 * モジュール名: 全体まとめ登録ボタン制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: 全体まとめ登録ボタン押下時に文字数チェック、本のタイトル検証、登録処理を行うクラス
 * 履歴:
 *   2025/06/15 鶴田凌 新規作成
 *   2025/07/05 鶴田凌 volumeIdを再取得するよう修正
 */
public class ControlSummaryRegistrationButton {
    /** 呼び出し元の Activity */
    private final Activity activity;
    /** 全体まとめ用 DAO */
    private final SummaryDao dao;
    /** ユーザ ID */
    private final String uid;
    /** 非同期実行用スレッドプール */
    private final ExecutorService executor;

    /**
     * コンストラクタ
     *
     * @param activity 呼び出し元の Activity
     * @param dao      SummaryDao
     * @param uid      ユーザ ID
     * @param executor 処理実行用スレッドプール
     */
    public ControlSummaryRegistrationButton(
            Activity activity,
            SummaryDao dao,
            String uid,
            ExecutorService executor
    ) {
        this.activity = activity;
        this.dao = dao;
        this.uid = uid;
        this.executor = executor;
    }

    /**
     * 全体まとめ登録ボタンにリスナーを設定する
     *
     * @param btnRegisterSummary 登録ボタン
     * @param summaryInput       全体まとめ入力欄
     * @param switchPublic       公開設定用スイッチ
     */
    public void bind(
            ImageButton btnRegisterSummary,
            EditText summaryInput,
            Switch switchPublic
    ) {
        btnRegisterSummary.setOnClickListener(v -> {
            String overall = summaryInput.getText().toString();
            if (overall.length() > 500) {
                new AlertDialog.Builder(activity)
                    .setTitle(R.string.error)
                    .setMessage("全体まとめ登録は500文字以内で入力してください")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
                return;
            }

            // DisplayHomeのハイライトメモ登録と同様に、登録時に初めてvolumeIdを決定
            DisplaySummary ds = (DisplaySummary) activity;
            String title = ds.getBookNameInput()
                             .getText()
                             .toString()
                             .trim();
            String volumeId = ds.findVolumeIdByTitle(title);
            if (volumeId.isEmpty()) {
                Toast.makeText(
                    activity,
                    "本の名前を正しく入力してください",
                    Toast.LENGTH_SHORT
                ).show();
                return;
            }

            executor.execute(() -> {
                boolean ok = new TransmitSummary(activity)
                        .transmitSummary(uid, volumeId, overall, switchPublic.isChecked());
                activity.runOnUiThread(() -> {
                    Toast.makeText(
                        activity,
                        ok ? "登録成功" : "登録失敗",
                        Toast.LENGTH_SHORT
                    ).show();
                    if (ok) activity.finish();
                });
            });
        });
    }
}
