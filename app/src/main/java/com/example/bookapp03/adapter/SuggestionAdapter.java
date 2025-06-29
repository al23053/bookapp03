package com.example.bookapp03.adapter;

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
 * 検索候補（サジェスト）をリスト形式で表示するためのRecyclerView用アダプターです。
 * ユーザーが検索バーに文字を入力する際に表示される候補リストの表示を管理します。
 */
public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder> {

    /**
     * 表示する検索候補の文字列リスト。
     */
    private List<String> suggestions;

    /**
     * 検索候補がクリックされたときに通知されるリスナー。
     */
    private OnSuggestionClickListener listener;

    /**
     * 検索候補のクリックイベントを処理するためのインターフェースです。
     */
    public interface OnSuggestionClickListener {
        /**
         * 検索候補がクリックされたときに呼び出されます。
         *
         * @param suggestion クリックされた検索候補の文字列
         */
        void onSuggestionClick(String suggestion);
    }

    /**
     * SuggestionAdapterのコンストラクタです。
     *
     * @param listener 検索候補のクリックリスナー
     */
    public SuggestionAdapter(OnSuggestionClickListener listener) {
        this.suggestions = new ArrayList<>();
        this.listener = listener;
    }

    /**
     * 検索候補のリストを更新し、RecyclerViewの表示を最新の状態にします。
     * 古いデータをクリアし、新しいデータを追加します。
     *
     * @param newSuggestions 新しい検索候補のリスト
     */
    public void updateSuggestions(List<String> newSuggestions) {
        this.suggestions.clear(); // 既存のデータをクリア
        this.suggestions.addAll(newSuggestions); // 新しいデータを追加
        notifyDataSetChanged(); // RecyclerView を更新
    }

    /**
     * 検索候補のリストを設定し、RecyclerViewの表示を更新します。
     * updateSuggestions と同様に動作しますが、nullを受け入れる点が異なります。
     *
     * @param newSuggestions 設定する新しい検索候補のリスト。nullの場合、リストはクリアされます。
     */
    public void setSuggestions(List<String> newSuggestions) {
        this.suggestions.clear();
        if (newSuggestions != null) {
            this.suggestions.addAll(newSuggestions);
        }
        notifyDataSetChanged();
    }

    /**
     * ViewHolderを作成します。レイアウトをインフレートし、ViewHolderを初期化します。
     *
     * @param parent   ViewHolderが属するViewGroup
     * @param viewType ビューのタイプ（使用しない）
     * @return 新しく作成されたSuggestionViewHolder
     */
    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    /**
     * ViewHolderにデータをバインドします。特定の位置の検索候補データをViewHolderのUI要素に設定します。
     *
     * @param holder   データをバインドするSuggestionViewHolder
     * @param position データを取得するリスト内の位置
     */
    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        String suggestion = suggestions.get(position);
        holder.suggestionTextView.setText(suggestion);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSuggestionClick(suggestion);
            }
        });
    }

    /**
     * アダプターが保持するアイテム（検索候補）の総数を返します。
     *
     * @return アイテムの総数
     */
    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    /**
     * RecyclerViewの各アイテム（検索候補）のビューを保持するViewHolderクラスです。
     */
    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        /**
         * 検索候補のテキストを表示するTextView。
         */
        TextView suggestionTextView;

        /**
         * SuggestionViewHolderのコンストラクタです。
         *
         * @param itemView アイテムのルートビュー
         */
        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            suggestionTextView = itemView.findViewById(R.id.suggestion_text_view);
        }
    }
}
