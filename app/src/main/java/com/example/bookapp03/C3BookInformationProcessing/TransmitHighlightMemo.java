package com.example.bookapp03.C3BookInformationProcessing;

import android.content.Context;
import android.util.Log;

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

    private static final String TAG = "TransmitHighlightMemo";
    
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
            Log.d(TAG, "=== ハイライトメモ送信開始 ===");
            Log.d(TAG, "UID: " + uid);
            Log.d(TAG, "VolumeID: " + volumeId);
            Log.d(TAG, "Page: " + data.getPage());
            Log.d(TAG, "Line: " + data.getLine());
            Log.d(TAG, "Memo: " + data.getMemo());
            
            RegisterHighlightMemo register = new RegisterHighlightMemo(context);

            // ❌ 修正前: 不要なデータ変換
            // HighlightMemoData modelData = new HighlightMemoData(
            //         data.getPage(),
            //         data.getLine(),
            //         data.getMemo()
            // );

            // ✅ 修正後: 受け取ったデータをそのまま使用
            boolean result = register.registerHighlightMemo(uid, volumeId, data);
            
            Log.d(TAG, "送信結果: " + result);
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "ハイライトメモ送信エラー", e);
            e.printStackTrace();
            return false;
        }
    }
}
