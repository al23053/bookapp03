package com.example.bookapp03.service;

import android.util.Log;

import com.example.bookapp03.model.Book;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Random;

/**
 * Firebase Firestoreから書籍情報を取得し、ユーザーの好みに合わせて分類するサービスです。
 * おすすめ書籍の取得ロジックを担当します。
 */
public class FirestoreBookService {

    private static final String TAG = "FirestoreBookService";
    private FirebaseFirestore db;
    private static final int MAX_RECOMMENDATION_COUNT = 10;
    private static final int FETCH_LIMIT_FOR_RANDOM = 30;

    /**
     * FirestoreBookServiceのコンストラクタです。
     * FirebaseFirestoreのインスタンスを初期化します。
     */
    public FirestoreBookService() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * 書籍の推薦結果を通知するためのコールバックインターフェースです。
     */
    public interface BookRecommendationCallback {
        /**
         * おすすめ書籍リストが正常に受信されたときに呼び出されます。
         *
         * @param matchingBooks    ユーザーの好きなジャンルに一致する書籍のリスト
         * @param nonMatchingBooks ユーザーの好きなジャンルに一致しない書籍のリスト
         */
        void onRecommendationsReceived(List<Book> matchingBooks, List<Book> nonMatchingBooks);

        /**
         * おすすめ書籍の取得中にエラーが発生したときに呼び出されます。
         *
         * @param errorMessage エラーメッセージ
         */
        void onFailure(String errorMessage);
    }

    /**
     * Firebase Firestoreから全ての書籍を取得し、ユーザーの好きなジャンルに基づいて分類します。
     * ランダムに取得した書籍の中から、好きなジャンルに合う本と合わない本を選定します。
     *
     * @param userFavoriteGenres ユーザーが好きなジャンルのリスト
     * @param callback           結果を通知するコールバック
     */
    public void getRecommendedBooksFromFirestore(List<String> userFavoriteGenres, BookRecommendationCallback callback) {
        Log.d(TAG, "Fetching all books from Firestore for recommendation.");
        Set<String> lowerCaseFavoriteGenres = new HashSet<>();
        if (userFavoriteGenres != null) {
            for (String genre : userFavoriteGenres) {
                lowerCaseFavoriteGenres.add(genre.toLowerCase(Locale.getDefault()));
            }
        }

        db.collection("books")
                .limit(FETCH_LIMIT_FOR_RANDOM)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Book> allFetchedBooks = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Book book = document.toObject(Book.class);
                            book.setId(document.getId());
                            allFetchedBooks.add(book);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing book document: " + document.getId(), e);
                        }
                    }

                    // フェッチした書籍をシャッフルし、ランダム性を確保
                    Collections.shuffle(allFetchedBooks);

                    List<Book> matchingBooksFull = new ArrayList<>();
                    List<Book> nonMatchingBooksFull = new ArrayList<>();

                    // 取得した全ての書籍を分類
                    for (Book book : allFetchedBooks) {
                        if (isBookMatchingAnyGenre(book, lowerCaseFavoriteGenres)) {
                            matchingBooksFull.add(book);
                        } else {
                            nonMatchingBooksFull.add(book);
                        }
                    }

                    // 最終的に返すリストの数をMAX_RECOMMENDATION_COUNTに制限
                    List<Book> finalMatchingBooks = new ArrayList<>();
                    for (int i = 0; i < MAX_RECOMMENDATION_COUNT && i < matchingBooksFull.size(); i++) {
                        finalMatchingBooks.add(matchingBooksFull.get(i));
                    }

                    List<Book> finalNonMatchingBooks = new ArrayList<>();
                    for (int i = 0; i < MAX_RECOMMENDATION_COUNT && i < nonMatchingBooksFull.size(); i++) {
                        finalNonMatchingBooks.add(nonMatchingBooksFull.get(i));
                    }

                    Log.d(TAG, "Final matching books selected: " + finalMatchingBooks.size());
                    Log.d(TAG, "Final non-matching books selected: " + finalNonMatchingBooks.size());

                    // 結果をコールバック経由で通知
                    callback.onRecommendationsReceived(finalMatchingBooks, finalNonMatchingBooks);
                })
                .addOnFailureListener(e -> {
                    // 書籍取得中にエラーが発生した場合
                    Log.e(TAG, "Error fetching books from Firestore: " + e.getMessage(), e);
                    callback.onFailure("Firestoreからの書籍取得中にエラーが発生しました: " + e.getMessage());
                });
    }

    /**
     * 指定された本が、ユーザーの好きなジャンルリストのいずれかにマッチするかどうかを判定します。
     * 大文字小文字を区別せず、カテゴリの完全一致で判定します。
     *
     * @param book                    判定対象の書籍オブジェクト
     * @param lowerCaseFavoriteGenres ユーザーの好きなジャンル（小文字に変換済み）のセット
     * @return マッチする場合はtrue、しない場合はfalse
     */
    private boolean isBookMatchingAnyGenre(Book book, Set<String> lowerCaseFavoriteGenres) {
        if (book.getCategories() == null || book.getCategories().isEmpty()) {
            return false;
        }
        // 書籍の各カテゴリをチェック
        for (String bookCategory : book.getCategories()) {
            if (lowerCaseFavoriteGenres.contains(bookCategory.toLowerCase(Locale.getDefault()))) {
                return true;
            }
        }
        return false;
    }
}
