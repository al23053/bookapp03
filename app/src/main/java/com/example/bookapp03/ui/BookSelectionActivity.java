package com.example.bookapp03.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.bookapp03.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * 書籍が選択された際に、その書籍のレビューが存在するかどうかを自動的にチェックし、
 * 適切な次の画面（レビュー一覧、レビューなし、本情報なし）へ遷移させるActivityです。
 * 通常、ユーザーには直接表示されず、バックグラウンドで処理を行います。
 */
public class BookSelectionActivity extends AppCompatActivity {

    private static final String TAG = "BookSelectionActivity";
    /**
     * Firebase Firestoreデータベースのインスタンス。
     */
    private FirebaseFirestore db;

    /**
     * Activityが最初に作成されるときに呼び出されます。
     * Firebaseの初期化、Intentから渡された本の情報の取得、
     * そしてその情報に基づいてレビューの存在チェックを開始します。
     *
     * @param savedInstanceState 以前に保存された状態データを含むBundleオブジェクト。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_selection);
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        // SearchResultActivityから渡された本のIDとタイトルを取得
        Intent intent = getIntent();
        String selectedBookId = intent.getStringExtra("bookId");
        String selectedBookTitle = intent.getStringExtra("bookTitle");

        if (selectedBookId != null && !selectedBookId.isEmpty()) {
            // 本のIDが有効であれば、すぐにデータベースチェックを実行
            Log.d(TAG, "BookSelectionActivity: 自動チェックを開始します。ID: " + selectedBookId + ", Title: " + selectedBookTitle);
            checkBookExistenceAndReviews(selectedBookId, selectedBookTitle);
        } else {
            // bookIdが渡されなかった場合はエラー表示または前の画面に戻る
            Toast.makeText(this, "本の情報が正しく渡されませんでした。", Toast.LENGTH_LONG).show();
            Log.e(TAG, "BookSelectionActivity: bookId が null または空です。");
            finish(); // このActivityを終了して前の画面に戻る
        }
    }

    /**
     * Firestoreに指定された書籍IDのドキュメントが存在するかどうかを確認します。
     * 存在しない場合やエラーが発生した場合は、BookNotFoundActivityへ遷移します。
     *
     * @param bookId    確認する書籍のID
     * @param bookTitle 確認する書籍のタイトル（ログや次の画面への引き渡し用）
     */
    private void checkBookExistenceAndReviews(String bookId, String bookTitle) {
        Log.d(TAG, "Checking book ID: " + bookId + " for title: " + bookTitle);
        DocumentReference bookRef = db.collection("books").document(bookId);

        // ドキュメントの存在を確認
        bookRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // 書籍ドキュメントが存在する場合
                    Log.d(TAG, "Book exists: " + bookTitle);
                    checkReviewsForExistingBook(bookId, bookTitle); // レビューの存在チェックに進む
                } else {
                    // 書籍ドキュメントが存在しない場合
                    Log.d(TAG, "Book does not exist in database: " + bookTitle);
                    navigateToBookNotFoundActivity(bookTitle); // 本が見つからなかったActivityへ遷移
                }
            } else {
                // ドキュメント取得中にエラーが発生した場合
                Log.e(TAG, "Error checking book existence: ", task.getException());
                Toast.makeText(this, "本の情報確認中にエラーが発生しました。", Toast.LENGTH_SHORT).show();
                navigateToBookNotFoundActivity(bookTitle); // エラーの場合も本が見つからなかったActivityへ遷移
            }
        });
    }

    /**
     * データベースに既存の書籍について、レビュー（"reviews"サブコレクション）が存在するかどうかを確認します。
     * レビューが存在すればUserReviewListActivityへ、存在しなければNoReviewsActivityへ遷移します。
     *
     * @param bookId    レビューを確認する書籍のID
     * @param bookTitle レビューを確認する書籍のタイトル
     */
    private void checkReviewsForExistingBook(String bookId, String bookTitle) {
        db.collection("books").document(bookId).collection("reviews")
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 結果がnullでなく、かつドキュメントが1件以上あればレビューが存在する
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            Log.d(TAG, "Reviews exist for book: " + bookTitle);
                            navigateToUserReviewList(bookId, bookTitle); // レビュー一覧画面へ遷移
                        } else {
                            // レビューが1件も存在しない場合
                            Log.d(TAG, "No reviews found for book: " + bookTitle);
                            navigateToNoReviewsActivity(bookId, bookTitle); // レビューなし画面へ遷移
                        }
                    } else {
                        // レビュー確認中にエラーが発生した場合
                        Log.e(TAG, "Error checking reviews: ", task.getException());
                        Toast.makeText(this, "レビューの確認中にエラーが発生しました。", Toast.LENGTH_SHORT).show();
                        navigateToNoReviewsActivity(bookId, bookTitle); // エラーの場合もレビューなし画面へ遷移
                    }
                });
    }

    /**
     * UserReviewListActivityへ遷移します。
     *
     * @param bookId    遷移先に渡す書籍のID
     * @param bookTitle 遷移先に渡す書籍のタイトル
     */
    private void navigateToUserReviewList(String bookId, String bookTitle) {
        Intent intent = new Intent(BookSelectionActivity.this, UserReviewListActivity.class);
        intent.putExtra("bookId", bookId);
        intent.putExtra("bookTitle", bookTitle);
        startActivity(intent);
        finish();
    }

    /**
     * NoReviewsActivityへ遷移します。
     *
     * @param bookId    遷移先に渡す書籍のID
     * @param bookTitle 遷移先に渡す書籍のタイトル
     */
    private void navigateToNoReviewsActivity(String bookId, String bookTitle) {
        Intent intent = new Intent(BookSelectionActivity.this, NoReviewsActivity.class);
        intent.putExtra("bookId", bookId);
        intent.putExtra("bookTitle", bookTitle);
        startActivity(intent);
        finish();
    }

    /**
     * BookNotFoundActivityへ遷移します。
     *
     * @param bookTitle 遷移先に渡す書籍のタイトル
     */
    private void navigateToBookNotFoundActivity(String bookTitle) {
        Intent intent = new Intent(BookSelectionActivity.this, BookNotFoundActivity.class);
        intent.putExtra("bookTitle", bookTitle);
        startActivity(intent);
        finish();
    }
}
