package com.example.bookapp03.C1UIProcessing;

import android.content.Context;
import android.content.Intent;
import android.widget.ImageButton;

/**
 * モジュール名: ハンバーガーバー制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: 右上のハンバーガーアイコン押下でハイライトメモ画面を起動するクラス
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
public class ControlHamburgerBar {
    private final Context context;
    private final String uid;
    private final String volumeId;

    /**
     * コンストラクタ
     *
     * @param context  呼び出し元 Context
     * @param uid      ログイン中のユーザID
     * @param volumeId 書籍ボリュームID
     */
    public ControlHamburgerBar(Context context, String uid, String volumeId) {
        this.context = context;
        this.uid = uid;
        this.volumeId = volumeId;
    }

    /**
     * ImageButton にリスナーを設定し、押下時に
     * DisplayHighlightMemo Activity を起動する。
     *
     * @param btnMenu ハンバーガーアイコンボタン
     */
    public void bind(ImageButton btnMenu) {
        btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(context, DisplayHighlightMemo.class);
            intent.putExtra("uid", uid);
            intent.putExtra("volumeId", volumeId);
            context.startActivity(intent);
        });
    }
}
