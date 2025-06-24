/**
 * モジュール名: BookDetailViewModel
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍詳細データの表示用ビューモデル
 * 履歴:
 * 2025/06/09 横山葉 新規作成
 */

package com.example.bookapp03.presentation.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp03.data.model.BookDetailData;
import com.example.bookapp03.data.model.HighlightMemoData;
import com.example.bookapp03.domain.repository.BookRepository;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 書籍の詳細情報を保持・管理するビューモデル
 */
public class BookDetailViewModel extends ViewModel { // ViewModelを継承

    private static final String TAG = "BookDetailViewModel";

    private final BookRepository bookRepository; // Repositoryのインスタンス
    private final MutableLiveData<BookDetailData> _bookDetail = new MutableLiveData<>();
    public LiveData<BookDetailData> bookDetail = _bookDetail; // UIが監視するLiveData

    private final MutableLiveData<List<HighlightMemoData>> _highlightMemos = new MutableLiveData<>();
    public LiveData<List<HighlightMemoData>> highlightMemos = _highlightMemos; // UIが監視するLiveData

    /**
     * コンストラクタ
     * @param bookRepository データリポジトリのインスタンス
     */
    public BookDetailViewModel(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * 書籍詳細データをロードする
     * @param uid ユーザーID
     * @param volumeId 書籍のボリュームID
     */
    public void loadBookDetail(String uid, String volumeId) {
        // 非同期でリポジトリからデータを取得
        Future<BookDetailData> future = bookRepository.getBookDetail(uid, volumeId);
        new Thread(() -> {
            try {
                BookDetailData detailData = future.get(); // 非同期処理の完了を待つ
                _bookDetail.postValue(detailData); // UIスレッドでLiveDataを更新
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error loading book detail for volumeId: " + volumeId + ", Error: " + e.getMessage());
                _bookDetail.postValue(null); // エラー時はnullをセット
            }
        }).start();
    }

    /**
     * 書籍詳細データが存在するかどうかを確認する
     * @return データが存在する場合 true、存在しない場合 false
     */
    public boolean hasDetail() {
        return _bookDetail.getValue() != null;
    }

    /**
     * ハイライトメモをロードする
     * @param uid ユーザーID
     * @param volumeId 書籍のボリュームID
     */
    public void loadHighlightMemos(String uid, String volumeId) {
        Future<List<HighlightMemoData>> future = bookRepository.getHighlightMemos(uid, volumeId);
        new Thread(() -> {
            try {
                List<HighlightMemoData> memos = future.get();
                _highlightMemos.postValue(memos);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error loading highlight memos for volumeId: " + volumeId + ", Error: " + e.getMessage());
                _highlightMemos.postValue(null);
            }
        }).start();
    }

    /**
     * ハイライトメモを登録する
     * @param uid ユーザーID
     * @param volumeId 書籍のボリュームID
     * @param memoData 登録するメモデータ
     */
    public void registerHighlightMemo(String uid, String volumeId, HighlightMemoData memoData) {
        Future<Boolean> future = bookRepository.registerHighlightMemo(uid, volumeId, memoData);
        new Thread(() -> {
            try {
                Boolean success = future.get();
                if (success != null && success) {
                    Log.d(TAG, "Highlight memo registered successfully.");
                    loadHighlightMemos(uid, volumeId); // 登録後、リストを再ロード
                } else {
                    Log.e(TAG, "Failed to register highlight memo.");
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error registering highlight memo: " + e.getMessage());
            }
        }).start();
    }
}