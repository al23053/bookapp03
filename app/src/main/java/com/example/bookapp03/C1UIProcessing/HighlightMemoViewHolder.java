package com.example.bookapp03.C1UIProcessing;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp03.R;

/**
 * モジュール名: ハイライトメモビューホルダー
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ハイライトメモアイテムのビューを保持し、データをバインドするクラス
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
public class HighlightMemoViewHolder
        extends RecyclerView.ViewHolder {

    /**
     * ページ・行情報表示用テキストビュー
     */
    private final TextView tvPageLine;

    /**
     * メモ内容表示用テキストビュー
     */
    private final TextView tvMemo;

    /**
     * コンストラクタ
     *
     * @param itemView ビューオブジェクト
     */
    public HighlightMemoViewHolder(@NonNull View itemView) {
        super(itemView);
        tvPageLine = itemView.findViewById(R.id.tvPageLine);
        tvMemo = itemView.findViewById(R.id.tvMemo);
    }

    /**
     * データをビューに反映する。
     *
     * @param data ハイライトメモデータ
     */
    public void bind(HighlightMemoData data) {
        tvPageLine.setText("P: " + data.getPage() + "  L: " + data.getLine());
        tvMemo.setText(data.getMemo());
    }
}