package com.example.bookapp03.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookapp03.C1UIProcessing.ControlBackButton;
import com.example.bookapp03.R;
import com.example.bookapp03.model.Review;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Locale;

/**
 * 特定の書籍に対するユーザーレビューの一覧を表示するActivityです。
 * FirestoreRecyclerAdapterを使用してFirestoreからリアルタイムでレビューデータを取得し、表示します。
 */
public class UserReviewListActivity extends AppCompatActivity {

    private static final String TAG = "UserReviewListActivity";

    /**
     * 検索結果画面に戻るためのボタン。
     */
    private Button backToSearchButton;
    /**
     * 現在表示している書籍のタイトルを表示するTextView。
     */
    private TextView bookTitleTextView;
    /**
     * 書籍のレビューリストを表示するためのRecyclerView。
     */
    private RecyclerView reviewsRecyclerView;

    /**
     * Firebase Firestoreデータベースのインスタンス。
     */
    private FirebaseFirestore db;
    /**
     * Firestoreからレビューデータを取得し、RecyclerViewにバインドするためのアダプター。
     */
    private FirestoreRecyclerAdapter<Review, ReviewViewHolder> adapter;

    /**
     * 前の画面から渡される本のID。Firestoreのクエリに使用されます。
     */
    private String bookId;
    /**
     * 前の画面から渡される本のタイトル。UI表示に使用されます。
     */
    private String bookTitle;

    /**
     * Activityが最初に作成されるときに呼び出されます。
     * レイアウトの設定、UI要素の初期化、本の情報の取得、RecyclerViewのセットアップを行います。
     *
     * @param savedInstanceState 以前に保存された状態データを含むBundleオブジェクト。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_review_list);

        // Firebase Firestoreインスタンスの取得
        db = FirebaseFirestore.getInstance();

        // UI要素の初期化
        bookTitleTextView = findViewById(R.id.bookTitleTextView);
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        backToSearchButton = findViewById(R.id.backToSearchButton);

        // Intentから本のIDとタイトルを取得
        bookId = getIntent().getStringExtra("bookId");
        bookTitle = getIntent().getStringExtra("bookTitle"); // ここでフィールドに代入

        Log.d("ReviewListDebug", "Received bookId in UserReviewListActivity: " + bookId); // ★ 追加
        // 本のIDが取得できない場合の処理
        if (bookId == null || bookId.isEmpty()) {
            Toast.makeText(this, "本の情報がありません。", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bookTitleTextView.setText(bookTitle != null ? bookTitle : "感想一覧");

        setupRecyclerView();

        ControlBackButton.setupBackButton(backToSearchButton, this);
    }

    /**
     * レビューを表示するためのRecyclerViewをセットアップします。
     * Firestoreのクエリを構築し、FirestoreRecyclerAdapterを初期化・設定します。
     */
    private void setupRecyclerView() {
        Query query = db.collection("summaries")
                .whereEqualTo("volumeId", bookId);
        FirestoreRecyclerOptions<Review> options = new FirestoreRecyclerOptions.Builder<Review>()
                .setQuery(query, Review.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<Review, ReviewViewHolder>(options) {
            /**
             * ViewHolderを作成します。レビューアイテムのレイアウトをインフレートします。
             * @param parent ViewHolderが属するViewGroup
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
             * ViewHolderにレビューデータをバインドします。
             * 各レビューアイテムにクリックリスナーを設定し、ボトムシートを表示するロジックを含みます。
             * @param holder データをバインドするReviewViewHolder
             * @param position リスト内のアイテムの位置
             * @param model データをバインドするReviewモデルオブジェクト
             */
            @Override
            protected void onBindViewHolder(@NonNull ReviewViewHolder holder, int position, @NonNull Review model) {
                String userId = model.getUid(); // Reviewモデルからuidを取得
                if (userId != null && !userId.isEmpty()) {
                    db.collection("users").document(userId).get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot userDocument = task.getResult();
                                    if (userDocument.exists()) {
                                        String nickname = userDocument.getString("nickname");
                                        if (nickname != null) {
                                            holder.usernameTextView.setText(nickname);
                                        } else {
                                            holder.usernameTextView.setText("名無しユーザー"); // nicknameがない場合
                                        }
                                    } else {
                                        holder.usernameTextView.setText("不明なユーザー"); // ユーザーが削除された場合など
                                    }
                                } else {
                                    Log.e(TAG, "Error getting user document: ", task.getException());
                                    holder.usernameTextView.setText("ユーザー名取得エラー");
                                }
                            });
                } else {
                    holder.usernameTextView.setText("ゲストユーザー"); // UIDがない場合
                }

                String comment = model.getComment();
                if (comment != null && comment.length() > 50) {
                    holder.commentTextView.setText(comment.substring(0, 50) + "...");
                } else {
                    holder.commentTextView.setText(comment);
                }

                holder.itemView.setOnClickListener(v -> {
                    ReviewDetailBottomSheetFragment bottomSheet = ReviewDetailBottomSheetFragment.newInstance(
                            model.getOverallSummary(),          // レビュー全文 (overallSummary)
                            holder.usernameTextView.getText().toString() // 既に取得済みのユーザー名
                    );
                    bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
                });
            }

            /**
             * FirestoreRecyclerAdapterでエラーが発生したときに呼び出されます。
             * @param e 発生したFirebaseFirestoreException
             */
            @Override
            public void onError(@NonNull com.google.firebase.firestore.FirebaseFirestoreException e) {
                Log.e(TAG, "FirestoreRecyclerAdapter: onError", e);
                Toast.makeText(UserReviewListActivity.this, "感想の読み込み中にエラーが発生しました。", Toast.LENGTH_SHORT).show();
            }

            /**
             * Firestoreのデータが変更され、アダプターが更新されたときに呼び出されます。
             * レビューがない場合にNoReviewsActivityへ遷移するロジックを含みます。
             */
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                Log.d(TAG, "onDataChanged: adapter has " + getItemCount() + " items.");
                if (getItemCount() == 0) {
                    Log.d(TAG, "No reviews found for bookId: " + bookId + ". Navigating to NoReviewsActivity.");
                    Intent intent = new Intent(UserReviewListActivity.this, NoReviewsActivity.class);
                    intent.putExtra("bookTitle", bookTitle);
                    intent.putExtra("bookId", bookId);
                    startActivity(intent);
                    finish();
                }
            }
        };

        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecyclerView.setAdapter(adapter);
    }

    /**
     * RecyclerViewの各アイテム（レビュー項目）のビューを保持するViewHolderクラスです。
     */
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        /**
         * レビュー投稿者のユーザー名を表示するTextView。
         */
        private final TextView usernameTextView;

        /**
         * レビューのコメント本文の概要を表示するTextView。
         */
        private final TextView commentTextView;

        /**
         * ReviewViewHolderのコンストラクタです。
         *
         * @param itemView アイテムのルートビュー
         */
        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
        }

        /**
         * 指定されたReviewデータをViewHolderのUI要素にバインドします。
         * コメントが長い場合は省略して表示します。
         *
         * @param review バインドするReviewオブジェクト
         */
        public void bind(@NonNull Review review) {
            String comment = review.getOverallSummary();
            if (comment != null && comment.length() > 50) {
                commentTextView.setText(comment.substring(0, 50) + "...");
            } else {
                commentTextView.setText(comment);
            }
        }
    }

    /**
     * Activityが表示され、ユーザーが操作できるようになるときに呼び出されます。
     * FirestoreRecyclerAdapterのリスニングを開始します。
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    /**
     * Activityがもはやユーザーに見えなくなるときに呼び出されます。
     * FirestoreRecyclerAdapterのリスニングを停止します。
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}
