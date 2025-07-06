/**
 * モジュール名: 本の一覧管理
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍一覧のデータを管理するViewModelクラス。UIに表示する書籍サマリのリストを提供し、
 * 書籍のロードや公開ステータスの更新などのデータ操作を仲介します。
 * 履歴:
 * 2025/06/09 横山葉 新規作成
 */
package com.example.bookapp03.C3BookInformationProcessing;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp03.C6BookInformationManaging.BookRepository;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 書籍一覧のデータを管理し、UIに提供するViewModelクラス。
 * データ層（Repository）とUI層（Activity/Fragment）の間の橋渡しを行います。
 */
public class BookListViewModel extends ViewModel {

    /** ログ出力用のタグ */
    private static final String TAG = "BookListViewModel";

    /** データ操作を行うリポジトリのインスタンス */
    private final BookRepository bookRepository;
    /** 書籍サマリのリストを保持し、UIに通知するためのMutableLiveData（内部用） */
    private final MutableLiveData<List<BookSummaryData>> _bookList = new MutableLiveData<>();
    /** UIが書籍リストの変更を監視するためのLiveData（公開用） */
    public LiveData<List<BookSummaryData>> bookList = _bookList;

    /**
     * BookListViewModelのコンストラクタ。
     * 必要なデータリポジトリのインスタンスを注入します。
     *
     * @param bookRepository データリポジトリのインスタンス
     */
    public BookListViewModel(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * 指定されたユーザーの書籍一覧サマリをリポジトリから非同期でロードする。
     * ロードが成功した場合、`bookList` LiveDataを更新する。
     *
     * @param uid データをロードするユーザーのID
     */
    public void loadBooks(String uid) {
        // 非同期でリポジトリから書籍サマリのリストを取得
        Future<List<BookSummaryData>> future = bookRepository.getAllBookSummaries(uid);
        new Thread(() -> {
            try {
                // 非同期処理の完了を待ち、結果を取得
                List<BookSummaryData> books = future.get();
                // UIスレッドでLiveDataを更新し、UIに通知
                _bookList.postValue(books);
            } catch (ExecutionException | InterruptedException e) {
                // データロード中のエラーをログに出力
                Log.e(TAG, "Error loading books for UID: " + uid + ", Error: " + e.getMessage());
                // エラー時はLiveDataにnullを設定してUIに通知
                _bookList.postValue(null);
            }
        }).start();
    }

    /**
     * 現在の書籍一覧が空であるかどうかを判定する。
     *
     * @return 書籍リストがnullであるか、または要素が1つも含まれていない場合はtrue、それ以外はfalse
     */
    public boolean isEmpty() {
        List<BookSummaryData> currentList = bookList.getValue();
        return currentList == null || currentList.isEmpty();
    }

    /**
     * 指定された書籍の公開ステータスを更新する。
     * 更新が成功した場合、`loadBooks`を呼び出して最新の書籍リストを再ロードする。
     *
     * @param uid             更新を行うユーザーのID
     * @param volumeId        対象の書籍のボリュームID
     * @param newPublicStatus 新しい公開状態（true: 公開, false: 非公開）
     */
    public void updatePublicStatus(String uid, String volumeId, boolean newPublicStatus) {
        // 非同期でリポジトリを介して書籍の公開ステータスを更新
        Future<Boolean> future = bookRepository.updateBookPublicStatus(uid, volumeId, newPublicStatus);
        new Thread(() -> {
            try {
                // 非同期処理の完了を待ち、更新の成否を取得
                Boolean success = future.get();
                if (success != null && success) {
                    // 更新成功をログに出力
                    Log.d(TAG, "Public status updated successfully for volumeId: " + volumeId);
                    // UIを最新の状態に保つため、書籍リストを再ロード
                    loadBooks(uid);
                } else {
                    // 更新失敗をログに出力
                    Log.e(TAG, "Failed to update public status for volumeId: " + volumeId);
                }
            } catch (ExecutionException | InterruptedException e) {
                // 更新処理中のエラーをログに出力
                Log.e(TAG, "Error updating public status for volumeId: " + volumeId + ", Error: " + e.getMessage());
            }
        }).start();
    }
}