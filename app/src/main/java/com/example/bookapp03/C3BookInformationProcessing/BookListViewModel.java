/**
 * モジュール名: BookListViewModel
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍一覧のデータを管理するViewModelクラス
 * 履歴:
 * 2025/06/09 横山葉 新規作成
 */

package com.example.bookapp03.C3BookInformationProcessing;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp03.C6BookInformationManaging.BookRepository; // 新しいimport

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 書籍一覧のデータ管理を行うViewModel
 */
public class BookListViewModel extends ViewModel { // ViewModelを継承

    private static final String TAG = "BookListViewModel";

    private final BookRepository bookRepository; // Repositoryのインスタンス
    private final MutableLiveData<List<BookSummaryData>> _bookList = new MutableLiveData<>();
    public LiveData<List<BookSummaryData>> bookList = _bookList; // UIが監視するLiveData

    /**
     * コンストラクタ
     * @param bookRepository データリポジトリのインスタンス
     */
    public BookListViewModel(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * 書籍一覧をロードする
     * @param uid ユーザーID
     */
    public void loadBooks(String uid) {
        // 非同期でリポジトリからデータを取得
        Future<List<BookSummaryData>> future = bookRepository.getAllBookSummaries(uid);
        new Thread(() -> {
            try {
                List<BookSummaryData> books = future.get(); // 非同期処理の完了を待つ
                _bookList.postValue(books); // UIスレッドでLiveDataを更新
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error loading books: " + e.getMessage());
                _bookList.postValue(null); // エラー時はnullをセット
            }
        }).start();
    }

    /**
     * 書籍一覧が空かどうかを判定する
     * @return true: 空, false: 空でない
     */
    public boolean isEmpty() {
        List<BookSummaryData> currentList = bookList.getValue();
        return currentList == null || currentList.isEmpty();
    }

    /**
     * 指定された書籍の公開状態を更新する
     * @param volumeId 対象の書籍のボリュームID
     * @param newPublicStatus 新しい公開状態
     */
    public void updatePublicStatus(String uid, String volumeId, boolean newPublicStatus) {
        // 非同期でリポジトリを介して公開ステータスを更新
        Future<Boolean> future = bookRepository.updateBookPublicStatus(uid, volumeId, newPublicStatus);
        new Thread(() -> {
            try {
                Boolean success = future.get(); // 非同期処理の完了を待つ
                if (success != null && success) {
                    Log.d(TAG, "Public status updated successfully for volumeId: " + volumeId);
                    // 更新成功後、リストを再ロードしてUIを更新
                    loadBooks(uid);
                } else {
                    Log.e(TAG, "Failed to update public status for volumeId: " + volumeId);
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error updating public status: " + e.getMessage());
            }
        }).start();
    }
}