package com.example.bookapp03.C1UIProcessing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.bookapp03.R;

import java.util.ArrayList;
import java.util.List;

/**
 * モジュール名: ハイライトメモ表示用アダプター
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: ハンバーガーバーが開かれたときにハイライトメモを表示するためのアダプタークラス
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */

public class HighlightMemoAdapter
        extends RecyclerView.Adapter<HighlightMemoAdapter.ViewHolder> {

    private final List<HighlightMemoData> items = new ArrayList<>();

    public void setItems(List<HighlightMemoData> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.highlightmemodisplay, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        HighlightMemoData d = items.get(pos);
        h.tvPageLine.setText("P: " + d.getPage() + "  L: " + d.getLine());
        h.tvMemo.setText(d.getMemo());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPageLine, tvMemo;

        ViewHolder(View v) {
            super(v);
            tvPageLine = v.findViewById(R.id.tvPageLine);
            tvMemo = v.findViewById(R.id.tvMemo);
        }
    }
}
