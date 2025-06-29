package com.example.bookapp03.C1UIProcessing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp03.R;

import java.util.ArrayList;
import java.util.List;

/**
 * モジュール名: ハイライトメモ表示アダプター
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: RecyclerView に対してハイライトメモデータを表示するアダプタークラス
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
public class HighlightMemoAdapter
        extends RecyclerView.Adapter<HighlightMemoViewHolder> {

    /**
     * 表示対象のハイライトメモデータリスト
     */
    private final List<HighlightMemoData> items = new ArrayList<>();

    /**
     * データを新規設定し、ビューを更新する。
     *
     * @param data 表示するハイライトメモデータ一覧
     */
    public void setItems(List<HighlightMemoData> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    /**
     * ビューホルダーを生成する。
     *
     * @param parent   親 ViewGroup
     * @param viewType ビュータイプ
     * @return ビューホルダー
     */
    @NonNull
    @Override
    public HighlightMemoViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.highlightmemodisplay, parent, false);
        return new HighlightMemoViewHolder(view);
    }

    /**
     * ビューホルダーにデータをバインドする。
     *
     * @param holder   ビューホルダー
     * @param position データ位置
     */
    @Override
    public void onBindViewHolder(
            @NonNull HighlightMemoViewHolder holder,
            int position
    ) {
        holder.bind(items.get(position));
    }

    /**
     * アイテム数を返す。
     *
     * @return リストのサイズ
     */
    @Override
    public int getItemCount() {
        return items.size();
    }
}
