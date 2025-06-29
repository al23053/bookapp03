package com.example.bookapp03.adapter; // あなたのパッケージ名に合わせてください

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar; // RatingBarのインポートを追加
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.example.bookapp03.R; // あなたのRクラスのパスに合わせてください
import com.example.bookapp03.model.Review; // あなたのReviewモデルのパスに合わせてください

/**
 * Firebase Firestoreから取得したレビューデータをRecyclerViewに表示するためのアダプターです。
 * FirestoreRecyclerAdapterを継承しており、リアルタイムなデータ同期が可能です。
 */
public class YourFirestoreRecyclerAdapter extends FirestoreRecyclerAdapter<Review, YourFirestoreRecyclerAdapter.ReviewViewHolder> {

    /**
     * リスト内のアイテムがクリックされたときに通知されるリスナー。
     */
    private OnItemClickListener listener;

    /**
     * リスト内のアイテムのクリックイベントを処理するためのインターフェースです。
     */
    public interface OnItemClickListener {
        /**
         * レビューアイテムがクリックされたときに呼び出されます。
         *
         * @param review クリックされたReviewオブジェクト
         */
        void onItemClick(Review review);
    }

    /**
     * YourFirestoreRecyclerAdapterのコンストラクタです。
     *
     * @param options FirestoreRecyclerAdapterのオプション（クエリやライフサイクルオーナーなど）
     */
    public YourFirestoreRecyclerAdapter(@NonNull FirestoreRecyclerOptions<Review> options) {
        super(options);
    }

    /**
     * クリックリスナーを設定します。
     *
     * @param listener 設定するOnItemClickListenerインスタンス
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * ViewHolderにデータをバインドします。
     * 特定の位置のReviewモデルデータがViewHolderのUI要素に設定されます。
     *
     * @param holder   データをバインドするReviewViewHolder
     * @param position リスト内のアイテムの位置
     * @param model    データをバインドするReviewモデルオブジェクト
     */
    @Override
    protected void onBindViewHolder(@NonNull ReviewViewHolder holder, int position, @NonNull Review model) {
        holder.commentTextView.setText(model.getComment());
        holder.reviewRatingBar.setRating(model.getRating());
        holder.usernameTextView.setText(model.getUsername());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(model); // クリックされたReviewオブジェクトを渡す
            }
        });
    }

    /**
     * ViewHolderを作成します。レイアウトをインフレートし、ViewHolderを初期化します。
     *
     * @param parent   ViewHolderが属するViewGroup
     * @param viewType ビューのタイプ（使用しない）
     * @return 新しく作成されたReviewViewHolder
     */
    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    /**
     * RecyclerViewの各アイテム（レビュー項目）のビューを保持するViewHolderクラスです。
     */
    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        /**
         * レビュー投稿者のユーザー名を表示するTextView。
         */
        private final TextView usernameTextView;
        /**
         * レビューの評価を表示するRatingBar。
         */
        private final RatingBar reviewRatingBar;
        /**
         * レビューのコメント本文を表示するTextView。
         */
        private final TextView commentTextView;

        /**
         * ReviewViewHolderのコンストラクタです。
         *
         * @param itemView アイテムのルートビュー
         */
        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            reviewRatingBar = itemView.findViewById(R.id.reviewRatingBar);
            commentTextView = itemView.findViewById(R.id.commentTextView);
        }
    }
}
