package com.example.bookapp03.C1UIProcessing;

import android.content.Context;

import com.example.bookapp03.C3BookInformationProcessing.TransmitHighlightMemo;

/**
 * モジュール名: ハイライト・メモ入力制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ページ・行・メモの入力を検証し、DB登録モジュールへ転送するクラス
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
public class ControlHighlightMemo {
    private final TransmitHighlightMemo transmitter;

    /**
     * コンストラクタ
     *
     * @param context  アプリケーションコンテキスト
     * @param uid      ログイン中のユーザID
     * @param volumeId 書籍ボリュームID
     */
    public ControlHighlightMemo(Context context, String uid, String volumeId) {
        this.transmitter = new TransmitHighlightMemo(context, uid, volumeId);
    }

    /**
     * ページ数・行数・メモの妥当性をチェックし、HighlightMemoData オブジェクトを生成する。
     *
     * @param page ページ数 (1–1000)
     * @param line 行数 (1–50)
     * @param memo メモ文字列（最大200文字）
     * @return バリデーション済みデータオブジェクト
     * @throws IllegalArgumentException 入力範囲外または文字数超過時
     */
    public HighlightMemoData getHighlightMemo(int page, int line, String memo) {
        if (page <= 0 || page > 1000 ) {
            throw new IllegalArgumentException("本のページ数が範囲を超えています");
        }
        if (line <= 0 || line > 50){
            throw new IllegalArgumentException("本の行数が範囲を超えています");
        }
        if (memo.length() > 200){
            throw new IllegalArgumentException("メモは200文字以内で入力してください");
        }
        return new HighlightMemoData(page, line, memo);
    }

    /**
     * バリデーション済みデータを DB 登録モジュールへ送信する。
     *
     * @param data ハイライトメモ情報オブジェクト
     * @return true=登録成功／false=登録失敗
     */
    public boolean sendData(HighlightMemoData data) {
        return transmitter.transmitHighlightMemo(data);
    }
}
