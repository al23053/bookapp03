package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
import android.widget.ImageButton;

/**
 * モジュール名: ホームへ戻るボタン制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: 戻るボタンを押したときにアクティビティを終了するクラス
 * 履歴:
 * 　2025/06/15 鶴田凌 新規作成
 */
public class ControlBackToHomeButton {

    /** Activity を保持し、finish() を呼び出すための参照 */
    private final Activity activity;

    /**
     * コンストラクタ
     *
     * @param activity このコントローラを利用する Activity
     */
    public ControlBackToHomeButton(Activity activity) {
        this.activity = activity;
    }

    /**
     * 指定した ImageButton を押下したときに Activity.finish() を実行するリスナーを設定する。
     *
     * @param btnBack 戻るボタンとして機能させる ImageButton
     */
    public void bind(ImageButton btnBack) {
        btnBack.setOnClickListener(v -> activity.finish());
    }
}