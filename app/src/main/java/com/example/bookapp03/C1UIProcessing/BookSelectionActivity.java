/**
 * モジュール名: BookSelectionActivity
 * 作成者: 三浦寛生
 * 作成日: 2025/06/15
 * 概要: 書籍が選択された際に、その書籍のレビューが存在するかどうかを自動的にチェックし、
 *   適切な次の画面（レビュー一覧、レビューなし、本情報なし）へ遷移させるActivityです。
 *  通常、ユーザーには直接表示されず、バックグラウンドで処理を行います。
 * 履歴:
 * 2025/06/15 三浦寛生 新規作成
 */
package com.example.bookapp03.C1UIProcessing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.bookapp03.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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

        Intent intent = getIntent();
        String selectedBookId = intent.getStringExtra("bookId");
        String selectedBookTitle = intent.getStringExtra("bookTitle");

        if (selectedBookId != null && !selectedBookId.isEmpty()) {
            Log.d(TAG, "BookSelectionActivity: 自動チェックを開始します。ID: " + selectedBookId + ", Title: " + selectedBookTitle);
            checkReviews(selectedBookId, selectedBookTitle);
        } else {
            Toast.makeText(this, "本の情報が正しく渡されませんでした。", Toast.LENGTH_LONG).show();
            Log.e(TAG, "BookSelectionActivity: bookId が null または空です。");
            finish();
        }
    }

    /**
     * Firestoreに指定された書籍IDのドキュメントが存在するかどうかを確認します。
     * 存在しない場合やエラーが発生した場合は、BookNotFoundActivityへ遷移します。
     *
     * @param bookId    確認する書籍のID
     * @param bookTitle 確認する書籍のタイトル（ログや次の画面への引き渡し用）
     */
    private void checkReviews(String bookId, String bookTitle) {
        Log.d(TAG, "Checking reviews in 'summaries' collection for volumeId: " + bookId);

        db.collection("summaries")
                .whereEqualTo("volumeId", bookId)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            Log.d(TAG, "Reviews found in 'summaries' for book: " + bookTitle);
                            navigateToUserReviewList(bookId, bookTitle);
                        } else {
                            Log.d(TAG, "No reviews found in 'summaries' for book: " + bookTitle);
                            navigateToNoReviewsActivity(bookId, bookTitle);
                        }
                    } else {
                        Log.e(TAG, "Error checking reviews in 'summaries': ", task.getException());
                        Toast.makeText(this, "レビューの確認中にエラーが発生しました。", Toast.LENGTH_SHORT).show();
                        navigateToNoReviewsActivity(bookId, bookTitle);
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
