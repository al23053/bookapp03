package com.example.bookapp03.C3BookInformationProcessing;

import android.content.Context;

import com.example.bookapp03.C1UIProcessing.HighlightMemoData;
import com.example.bookapp03.C6BookInformationManaging.RegisterHighlightMemo;

/**
 * モジュール名: ハイライトメモ送信処理
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: UI層から受け取ったハイライトメモデータを管理層に送信する
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
public class TransmitHighlightMemo {

    private Context context;
    private String uid;
    private String volumeId;

    /**
     * コンストラクタ
     *
     * @param context  Android コンテキスト
     * @param uid      ユーザID
     * @param volumeId 書籍ボリュームID
     */
    public TransmitHighlightMemo(Context context, String uid, String volumeId) {
        this.context = context.getApplicationContext();
        this.uid = uid;
        this.volumeId = volumeId;
    }

    /**
     * ハイライトメモを送信・登録する
     *
     * @param data ハイライトメモデータ（UI層）
     * @return 登録成功時 true、失敗時 false
     */
    public boolean transmitHighlightMemo(HighlightMemoData data) {
        try {
            RegisterHighlightMemo register = new RegisterHighlightMemo(context);

            // UI層のデータを管理層のデータ型に変換
            HighlightMemoData modelData =
                    new HighlightMemoData(
                            data.getPage(),
                            data.getLine(),
                            data.getMemo()
                    );

            return register.registerHighlightMemo(uid, volumeId, modelData);
        } catch (Exception e) {
            return false;
        }
    }
}
