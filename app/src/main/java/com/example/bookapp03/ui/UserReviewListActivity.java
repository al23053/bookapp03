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
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.bookapp03.R;
import com.example.bookapp03.model.Review;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
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

        // 本のIDが取得できない場合の処理
        if (bookId == null || bookId.isEmpty()) {
            Toast.makeText(this, "本の情報がありません。", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bookTitleTextView.setText(bookTitle != null ? bookTitle : "感想一覧");

        setupRecyclerView();

        backToSearchButton.setOnClickListener(v -> {
            Log.d(TAG, "「検索結果に戻る」ボタンがクリックされました。");
            finish();
        });
    }

    /**
     * レビューを表示するためのRecyclerViewをセットアップします。
     * Firestoreのクエリを構築し、FirestoreRecyclerAdapterを初期化・設定します。
     */
    private void setupRecyclerView() {
        Query query = db.collection("books")
                .document(bookId)
                .collection("reviews")
                .orderBy("timestamp", Query.Direction.DESCENDING);
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
                holder.bind(model);

                holder.itemView.setOnClickListener(v -> {
                    long timestampMillis = model.getTimestamp() != null ? model.getTimestamp().toDate().getTime() : 0L;

                    ReviewDetailBottomSheetFragment bottomSheet = ReviewDetailBottomSheetFragment.newInstance(
                            model.getComment(),      // レビュー全文
                            model.getRating(),       // 評価
                            model.getUsername(),     // 投稿者名
                            timestampMillis          // タイムスタンプ (long型)
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
         * レビューの評価を表示するRatingBar。
         */
        private final RatingBar reviewRatingBar;
        /**
         * レビューのコメント本文の概要を表示するTextView。
         */
        private final TextView commentTextView;
        /**
         * レビューの投稿日時を表示するTextView。
         */
        private final TextView timestampTextView;

        /**
         * ReviewViewHolderのコンストラクタです。
         *
         * @param itemView アイテムのルートビュー
         */
        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            reviewRatingBar = itemView.findViewById(R.id.reviewRatingBar);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }

        /**
         * 指定されたReviewデータをViewHolderのUI要素にバインドします。
         * コメントが長い場合は省略して表示します。
         *
         * @param review バインドするReviewオブジェクト
         */
        public void bind(@NonNull Review review) {
            usernameTextView.setText(review.getUsername());
            reviewRatingBar.setRating(review.getRating());
            String comment = review.getComment();
            if (comment != null && comment.length() > 50) {
                commentTextView.setText(comment.substring(0, 50) + "...");
            } else {
                commentTextView.setText(comment);
            }

            if (review.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN);
                String formattedDate = sdf.format(new Date(review.getTimestamp().toDate().getTime()));
                timestampTextView.setText(formattedDate);
            } else {
                timestampTextView.setText("");
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
