/**
 * モジュール名: YourFirestoreRecyclerAdapter
 * 作成者: 三浦寛生
 * 作成日: 2025/06/30
 * 概要:　Firebase Firestoreから取得したレビューデータをRecyclerViewに表示するためのアダプターです。
 * FirestoreRecyclerAdapterを継承しており、リアルタイムなデータ同期が可能です。
 * 履歴:
 * 2025/06/30 三浦寛生 新規作成
 */
package com.example.bookapp03.C1UIProcessing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.example.bookapp03.R;
import com.example.bookapp03.C3BookInformationProcessing.Review;
import com.google.firebase.firestore.FirebaseFirestore;

public class YourFirestoreRecyclerAdapter extends FirestoreRecyclerAdapter<Review, YourFirestoreRecyclerAdapter.ReviewViewHolder> {

    private FirebaseFirestore db;
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
     * ViewHolderにデータをバインドします。
     * 特定の位置のReviewモデルデータがViewHolderのUI要素に設定されます。
     *
     * @param holder   データをバインドするReviewViewHolder
     * @param position リスト内のアイテムの位置
     * @param model    データをバインドするReviewモデルオブジェクト
     */
    @Override
    protected void onBindViewHolder(@NonNull ReviewViewHolder holder, int position, @NonNull Review model) {
        String comment = model.getOverallSummary();
        if (comment == null || comment.trim().isEmpty()) {
            holder.commentTextView.setText("（本文なし）");
            holder.commentTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
        } else {
            holder.commentTextView.setText(comment);
            holder.commentTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
        }

        String uid = model.getUid();
        if (uid != null && !uid.isEmpty()) {
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nickname = documentSnapshot.getString("nickname");
                            if (nickname != null && !nickname.trim().isEmpty()) {
                                holder.usernameTextView.setText(nickname);
                                holder.usernameTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
                            } else {
                                holder.usernameTextView.setText("匿名ユーザー (ニックネーム未設定)");
                                holder.usernameTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                            }
                        } else {
                            holder.usernameTextView.setText("不明なユーザー");
                            holder.usernameTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                        }
                    })
                    .addOnFailureListener(e -> {
                        holder.usernameTextView.setText("ニックネーム取得エラー");
                        holder.usernameTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                    });
        } else {
            holder.usernameTextView.setText("不明なユーザー (UIDなし)");
            holder.usernameTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(model);
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
            commentTextView = itemView.findViewById(R.id.commentTextView);
        }
    }
}
