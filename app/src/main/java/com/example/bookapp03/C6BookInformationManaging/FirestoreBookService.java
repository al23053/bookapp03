/**
 * モジュール名: FirestoreBookService
 * 作成者: 三浦寛生
 * 作成日: 2025/06/30
 * 概要:　Firebase Firestoreから書籍情報を取得し、ユーザーの好みに合わせて分類するサービスです。
 * おすすめ書籍の取得ロジックを担当します。
 * 履歴:
 * 2025/06/30 三浦寛生 新規作成
 */
package com.example.bookapp03.C6BookInformationManaging;

import android.util.Log;

import com.example.bookapp03.C4SearchProcessing.Book;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.HttpUrl;
import androidx.annotation.NonNull;

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
        Log.d(TAG, "User Favorite Genres (lowercase): " + lowerCaseFavoriteGenres);


        db.collection("summaries")
                .limit(FETCH_LIMIT_FOR_RANDOM)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No documents found in 'summaries' collection.");
                        callback.onRecommendationsReceived(new ArrayList<>(), new ArrayList<>());
                        return;
                    }

                    final int totalDocuments = queryDocumentSnapshots.size();
                    final AtomicInteger processedDocumentCount = new AtomicInteger(0);

                    final Set<Book> uniqueBooksFromGoogle = Collections.synchronizedSet(new HashSet<>());
                    final Set<String> fetchingOrFetchedVolumeIds = Collections.synchronizedSet(new HashSet<>());


                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String volumeId = document.getString("volumeId");
                        String overallSummary = document.getString("overallSummary");

                        if (volumeId != null && !volumeId.isEmpty()) {
                            if (fetchingOrFetchedVolumeIds.contains(volumeId)) {
                                Log.d(TAG, "Skipping duplicate volumeId: " + volumeId + " from Firestore document: " + document.getId() + " (already processing/processed).");
                                if (processedDocumentCount.incrementAndGet() == totalDocuments) {
                                    processAndReturnBooks(new ArrayList<>(uniqueBooksFromGoogle), lowerCaseFavoriteGenres, callback);
                                }
                                continue;
                            }
                            fetchingOrFetchedVolumeIds.add(volumeId);

                            fetchBookDetailsFromGoogleBooksAsync(volumeId, overallSummary, new GoogleBooksApiCallback() {
                                @Override
                                public void onSuccess(Book book) {
                                    if (book != null) {
                                        if (uniqueBooksFromGoogle.add(book)) {
                                            Log.d(TAG, "Successfully fetched and enriched unique book: " + book.getTitle() + ", Book ID: " + book.getId());
                                        } else {
                                            Log.d(TAG, "Attempted to add duplicate book to uniqueBooksFromGoogle Set: " + book.getTitle() + ", Book ID: " + book.getId());
                                        }
                                    } else {
                                        Log.w(TAG, "Could not fetch Google Books API details for volumeId: " + volumeId + ". Book is null.");
                                    }
                                    if (processedDocumentCount.incrementAndGet() == totalDocuments) {
                                        processAndReturnBooks(new ArrayList<>(uniqueBooksFromGoogle), lowerCaseFavoriteGenres, callback);
                                    }
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    Log.e(TAG, "Error fetching book details for volumeId " + volumeId + ": " + errorMessage);
                                    if (processedDocumentCount.incrementAndGet() == totalDocuments) {
                                        processAndReturnBooks(new ArrayList<>(uniqueBooksFromGoogle), lowerCaseFavoriteGenres, callback);
                                    }
                                }
                            });
                        } else {
                            Log.w(TAG, "Firestore document " + document.getId() + " has no valid volumeId. Skipping this document.");
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
                            book.setOverallSummary(overallSummaryFromFirestore);

                            Log.d(TAG, "Fetched Book Categories from Google API for '" + book.getTitle() + "' (ID: " + book.getId() + "): " + book.getCategories());

                            callback.onSuccess(book);
                            return;
                        }
                    }
                    Log.e(TAG, "Google Books API call failed or response malformed for volumeId " + volumeId + ": " + (googleResponse != null ? googleResponse.code() + " " + googleResponse.message() : "null response"));
                    callback.onSuccess(null);
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
        Collections.shuffle(uniqueFetchedBooks);

        List<Book> matchingBooksFull = new ArrayList<>();
        List<Book> nonMatchingBooksFull = new ArrayList<>();

        for (Book book : uniqueFetchedBooks) {
            Log.d(TAG, "Comparing Book Categories: " + book.getCategories() + " with User Favorite Genres: " + lowerCaseFavoriteGenres + " for book: " + book.getTitle());

            if (book.getCategories() != null && !book.getCategories().isEmpty() &&
                    isBookMatchingAnyGenre(book, lowerCaseFavoriteGenres)) {
                matchingBooksFull.add(book);
            } else {
                nonMatchingBooksFull.add(book);
            }
        }

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
            Log.d(TAG, "Book '" + book.getTitle() + "' has no categories. Not matching.");
            return false;
        }
        for (String bookCategory : book.getCategories()) {
            String lowerCaseBookCategory = bookCategory.toLowerCase(Locale.getDefault());
            Log.d(TAG, "  - Checking book category: '" + bookCategory + "' (lower: '" + lowerCaseBookCategory + "') against user genres.");

            for (String favoriteGenre : lowerCaseFavoriteGenres) {
                if (lowerCaseBookCategory.contains(favoriteGenre)) {
                    Log.d(TAG, "  - MATCH FOUND! Book category '" + bookCategory + "' contains user favorite genre '" + favoriteGenre + "'.");
                    return true;
                }
            }
        }
        Log.d(TAG, "No matching genres found for book '" + book.getTitle() + "'.");
        return false;
    }
}