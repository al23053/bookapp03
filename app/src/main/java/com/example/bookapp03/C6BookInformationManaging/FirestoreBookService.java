package com.example.bookapp03.C6BookInformationManaging;

import android.util.Log;

import com.example.bookapp03.model.Book;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet; // 追加
import java.util.List;
import java.util.Locale;
import java.util.Set; // 追加
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.HttpUrl;
import androidx.annotation.NonNull;

/**
 * Firebase Firestoreから書籍情報を取得し、ユーザーの好みに合わせて分類するサービスです。
 * おすすめ書籍の取得ロジックを担当します。
 */
public class FirestoreBookService {

    private static final String TAG = "FirestoreBookService";
    private FirebaseFirestore db;
    private static final int MAX_RECOMMENDATION_COUNT = 10;
    private static final int FETCH_LIMIT_FOR_RANDOM = 30;

    private static final String GOOGLE_BOOKS_API_BASE_URL = "https://www.googleapis.com/books/v1/volumes";
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String googleBooksApiKey;

    public FirestoreBookService(OkHttpClient httpClient, Gson gson, String googleBooksApiKey) {
        this.db = FirebaseFirestore.getInstance();
        this.httpClient = httpClient;
        this.gson = gson;
        this.googleBooksApiKey = googleBooksApiKey;
    }

    public interface BookRecommendationCallback {
        void onRecommendationsReceived(List<Book> matchingBooks, List<Book> nonMatchingBooks);
        void onFailure(String errorMessage);
    }

    private interface GoogleBooksApiCallback {
        void onSuccess(Book book);
        void onFailure(String errorMessage);
    }

    public void getRecommendedBooksFromFirestore(List<String> userFavoriteGenres, BookRecommendationCallback callback) {
        Log.d(TAG, "Fetching books from Firestore for recommendation.");
        Set<String> lowerCaseFavoriteGenres = new HashSet<>();
        if (userFavoriteGenres != null) {
            for (String genre : userFavoriteGenres) {
                lowerCaseFavoriteGenres.add(genre.toLowerCase(Locale.getDefault()));
            }
        }

        db.collection("summaries")
                .limit(FETCH_LIMIT_FOR_RANDOM)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No documents found in 'summaries' collection.");
                        callback.onRecommendationsReceived(new ArrayList<>(), new ArrayList<>());
                        return;
                    }

                    final int totalDocuments = queryDocumentSnapshots.size(); // Firestoreから取得した全ドキュメント数
                    final AtomicInteger processedDocumentCount = new AtomicInteger(0); // 処理済みのドキュメント数

                    // ★★★ 修正: 重複排除のためにSetを導入 ★★★
                    // Google Books APIから取得したユニークなBookオブジェクトを格納
                    final Set<Book> uniqueBooksFromGoogle = Collections.synchronizedSet(new HashSet<>());
                    // ★★★ 追加: 既にGoogle Books APIの検索にかけたvolumeIdを記録するSet ★★★
                    // 同じvolumeIdの重複API呼び出しを防ぐ
                    final Set<String> fetchingOrFetchedVolumeIds = Collections.synchronizedSet(new HashSet<>());


                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String volumeId = document.getString("volumeId");
                        String overallSummary = document.getString("overallSummary");

                        if (volumeId != null && !volumeId.isEmpty()) {
                            // ★★★ 修正: 既にこのvolumeIdが処理中または処理済みであればスキップ ★★★
                            if (fetchingOrFetchedVolumeIds.contains(volumeId)) {
                                Log.d(TAG, "Skipping duplicate volumeId: " + volumeId + " from Firestore document: " + document.getId() + " (already processing/processed).");
                                // スキップした場合も全体の完了カウントを進める
                                if (processedDocumentCount.incrementAndGet() == totalDocuments) {
                                    // 全てのFirestoreドキュメントの処理が完了したら、ユニークな本を渡す
                                    processAndReturnBooks(new ArrayList<>(uniqueBooksFromGoogle), lowerCaseFavoriteGenres, callback);
                                }
                                continue; // このドキュメントの処理はスキップ
                            }
                            fetchingOrFetchedVolumeIds.add(volumeId); // このvolumeIdを処理中としてマーク

                            fetchBookDetailsFromGoogleBooksAsync(volumeId, overallSummary, new GoogleBooksApiCallback() {
                                @Override
                                public void onSuccess(Book book) {
                                    if (book != null) {
                                        // Book.equals() と Book.hashCode() が id に基づいているため、SetはIDで重複を判断する
                                        if (uniqueBooksFromGoogle.add(book)) { // Setへの追加が成功すればユニーク
                                            Log.d(TAG, "Successfully fetched and enriched unique book: " + book.getTitle() + ", Book ID: " + book.getId());
                                        } else {
                                            Log.d(TAG, "Attempted to add duplicate book to uniqueBooksFromGoogle Set: " + book.getTitle() + ", Book ID: " + book.getId());
                                        }
                                    } else {
                                        Log.w(TAG, "Could not fetch Google Books API details for volumeId: " + volumeId + ". Book is null.");
                                    }
                                    // このFirestoreドキュメントの処理が完了
                                    if (processedDocumentCount.incrementAndGet() == totalDocuments) {
                                        // 全てのFirestoreドキュメントの処理が完了したら、ユニークな本を渡す
                                        processAndReturnBooks(new ArrayList<>(uniqueBooksFromGoogle), lowerCaseFavoriteGenres, callback);
                                    }
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    Log.e(TAG, "Error fetching book details for volumeId " + volumeId + ": " + errorMessage);
                                    // エラーが発生した場合も、このFirestoreドキュメントの処理が完了したと見なす
                                    if (processedDocumentCount.incrementAndGet() == totalDocuments) {
                                        // 全てのFirestoreドキュメントの処理が完了したら、ユニークな本を渡す
                                        processAndReturnBooks(new ArrayList<>(uniqueBooksFromGoogle), lowerCaseFavoriteGenres, callback);
                                    }
                                }
                            });
                        } else {
                            Log.w(TAG, "Firestore document " + document.getId() + " has no valid volumeId. Skipping this document.");
                            // volumeIdがない場合も、このFirestoreドキュメントの処理が完了したと見なす
                            if (processedDocumentCount.incrementAndGet() == totalDocuments) {
                                processAndReturnBooks(new ArrayList<>(uniqueBooksFromGoogle), lowerCaseFavoriteGenres, callback);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching documents from 'summaries' collection in Firestore: " + e.getMessage(), e);
                    callback.onFailure("Firestoreからの書籍概要取得中にエラーが発生しました: " + e.getMessage());
                });
    }

    private void fetchBookDetailsFromGoogleBooksAsync(String volumeId, String overallSummaryFromFirestore, GoogleBooksApiCallback callback) {
        if (volumeId == null || volumeId.isEmpty()) {
            Log.w(TAG, "fetchBookDetailsFromGoogleBooksAsync called with empty or null volumeId. Cannot fetch book details.");
            callback.onSuccess(null);
            return;
        }

        HttpUrl.Builder googleUrlBuilder = HttpUrl.parse(GOOGLE_BOOKS_API_BASE_URL).newBuilder();
        googleUrlBuilder.addPathSegment(volumeId);
        googleUrlBuilder.addQueryParameter("key", googleBooksApiKey);

        String googleBooksApiUrl = googleUrlBuilder.build().toString();
        Log.d(TAG, "Google Books API URL for volumeId " + volumeId + ": " + googleBooksApiUrl);
        Request googleRequest = new Request.Builder().url(googleBooksApiUrl).build();

        httpClient.newCall(googleRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Google Books API communication error for volumeId " + volumeId + ": " + e.getMessage(), e);
                callback.onFailure("Google Books API通信エラー: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response googleResponse) throws IOException {
                try {
                    if (googleResponse.isSuccessful() && googleResponse.body() != null) {
                        String googleResponseBody = googleResponse.body().string();
                        Log.d(TAG, "Google Books API Raw Response for volumeId " + volumeId + ": " + googleResponseBody);

                        JsonObject googleJson = JsonParser.parseString(googleResponseBody).getAsJsonObject();
                        JsonObject volumeInfo = googleJson.getAsJsonObject("volumeInfo");

                        if (volumeInfo != null) {
                            Book book = new Book();
                            // ここでGoogle Books APIのボリュームIDをBookのIDとして設定
                            book.setId(volumeId);

                            book.setTitle(volumeInfo.has("title") ? volumeInfo.get("title").getAsString() : null);

                            if (volumeInfo.has("authors")) {
                                JsonArray authorsArray = volumeInfo.getAsJsonArray("authors");
                                if (authorsArray != null && authorsArray.size() > 0) {
                                    book.setAuthor(authorsArray.get(0).getAsString());
                                }
                            }
                            book.setPublishedDate(volumeInfo.has("publishedDate") ? volumeInfo.get("publishedDate").getAsString() : null);
                            book.setDescription(volumeInfo.has("description") ? volumeInfo.get("description").getAsString() : null);
                            book.setInfoLink(volumeInfo.has("infoLink") ? volumeInfo.get("infoLink").getAsString() : null);

                            if (volumeInfo.has("categories")) {
                                JsonArray categoriesArray = volumeInfo.getAsJsonArray("categories");
                                List<String> categories = new ArrayList<>();
                                if (categoriesArray != null) {
                                    for (int i = 0; i < categoriesArray.size(); i++) {
                                        categories.add(categoriesArray.get(i).getAsString());
                                    }
                                }
                                book.setCategories(categories);
                            }

                            if (volumeInfo.has("imageLinks")) {
                                JsonObject imageLinks = volumeInfo.getAsJsonObject("imageLinks");
                                String thumbnailUrl = null;
                                // 可能な限り高解像度の画像を取得
                                if (imageLinks.has("extraLarge")) {
                                    thumbnailUrl = imageLinks.get("extraLarge").getAsString();
                                } else if (imageLinks.has("large")) {
                                    thumbnailUrl = imageLinks.get("large").getAsString();
                                } else if (imageLinks.has("medium")) {
                                    thumbnailUrl = imageLinks.get("medium").getAsString();
                                } else if (imageLinks.has("small")) {
                                    thumbnailUrl = imageLinks.get("small").getAsString();
                                } else if (imageLinks.has("thumbnail")) {
                                    thumbnailUrl = imageLinks.get("thumbnail").getAsString();
                                }
                                book.setThumbnailUrl(thumbnailUrl);
                            }

                            if (volumeInfo.has("industryIdentifiers")) {
                                JsonArray identifiers = volumeInfo.getAsJsonArray("industryIdentifiers");
                                for (int i = 0; i < identifiers.size(); i++) {
                                    JsonObject identifier = identifiers.get(i).getAsJsonObject();
                                    String type = identifier.has("type") ? identifier.get("type").getAsString() : null;
                                    String value = identifier.has("identifier") ? identifier.get("identifier").getAsString() : null;
                                    if (type != null && value != null && (type.equals("ISBN_13") || type.equals("ISBN_10"))) {
                                        book.setIsbn(value);
                                        break;
                                    }
                                }
                            }
                            // Firestoreから取得したoverallSummaryを設定
                            book.setOverallSummary(overallSummaryFromFirestore);

                            callback.onSuccess(book);
                            return;
                        }
                    }
                    Log.e(TAG, "Google Books API call failed or response malformed for volumeId " + volumeId + ": " + (googleResponse != null ? googleResponse.code() + " " + googleResponse.message() : "null response"));
                    callback.onSuccess(null); // Bookオブジェクトが生成できなかった場合はnullを返す
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing Google Books API response for volumeId " + volumeId + ": " + e.getMessage(), e);
                    callback.onFailure("Google Books APIレスポンス解析エラー: " + e.getMessage());
                } finally {
                    if (googleResponse != null && googleResponse.body() != null) {
                        googleResponse.body().close();
                    }
                }
            }
        });
    }

    private void processAndReturnBooks(List<Book> uniqueFetchedBooks, Set<String> lowerCaseFavoriteGenres, BookRecommendationCallback callback) {
        // uniqueFetchedBooks は既にユニークなBookオブジェクトのリストになっている
        Collections.shuffle(uniqueFetchedBooks); // ランダムにシャッフル

        List<Book> matchingBooksFull = new ArrayList<>();
        List<Book> nonMatchingBooksFull = new ArrayList<>();

        for (Book book : uniqueFetchedBooks) { // ユニークな本だけを処理
            if (book.getCategories() != null && !book.getCategories().isEmpty() &&
                    isBookMatchingAnyGenre(book, lowerCaseFavoriteGenres)) {
                matchingBooksFull.add(book);
            } else {
                nonMatchingBooksFull.add(book);
            }
        }

        // 各リストから最大MAX_RECOMMENDATION_COUNT件を選択
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

        callback.onRecommendationsReceived(finalMatchingBooks, finalNonMatchingBooks);
    }

    private boolean isBookMatchingAnyGenre(Book book, Set<String> lowerCaseFavoriteGenres) {
        if (book.getCategories() == null || book.getCategories().isEmpty()) {
            return false;
        }
        for (String bookCategory : book.getCategories()) {
            if (lowerCaseFavoriteGenres.contains(bookCategory.toLowerCase(Locale.getDefault()))) {
                return true;
            }
        }
        return false;
    }
}