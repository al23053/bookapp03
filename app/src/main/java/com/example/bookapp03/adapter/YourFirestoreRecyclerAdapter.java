package com.example.bookapp03.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.example.bookapp03.R;
import com.example.bookapp03.model.Review;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Firebase Firestoreから取得したレビューデータをRecyclerViewに表示するためのアダプターです。
 * FirestoreRecyclerAdapterを継承しており、リアルタイムなデータ同期が可能です。
 */
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
        String comment = model.getOverallSummary(); // Reviewモデルのフィールド名に合わせて getOverallSummary() を使用
        if (comment == null || comment.trim().isEmpty()) {
            holder.commentTextView.setText("（本文なし）"); // または「レビューがまだ書かれていません」など
            holder.commentTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray)); // 任意の色付け
        } else {
            holder.commentTextView.setText(comment);
            holder.commentTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black)); // 元の色に戻す
        }

        String uid = model.getUid(); // Reviewモデルからuidを取得
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
                            // ユーザーIDに対応するドキュメントが存在しない場合
                            holder.usernameTextView.setText("不明なユーザー");
                            holder.usernameTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                        }
                    })
                    .addOnFailureListener(e -> {
                        // ニックネーム取得中にエラーが発生した場合
                        holder.usernameTextView.setText("ニックネーム取得エラー");
                        holder.usernameTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                    });
        } else {
            // UIDがnullまたは空の場合
            holder.usernameTextView.setText("不明なユーザー (UIDなし)");
            holder.usernameTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
        }

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
