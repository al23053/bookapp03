/**
 * モジュール名: 本の詳細情報管理
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍詳細データの表示および関連するハイライトメモの管理を行うViewModel。
 * 履歴:
 * 2025/06/09 横山葉 新規作成
 */
package com.example.bookapp03.C3BookInformationProcessing;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookapp03.C1UIProcessing.HighlightMemoData;
import com.example.bookapp03.C6BookInformationManaging.BookRepository;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 書籍の詳細情報および関連するハイライトメモのデータを保持・管理するViewModel。
 * UIに対するデータの提供と、データ層への操作要求を仲介します。
 */
public class BookDetailViewModel extends ViewModel {

    /** ログ出力用のタグ */
    private static final String TAG = "BookDetailViewModel";

    /** データ操作を行うリポジトリのインスタンス */
    private final BookRepository bookRepository;
    /** 書籍詳細データを保持し、UIに通知するためのMutableLiveData（内部用） */
    private final MutableLiveData<BookDetailData> _bookDetail = new MutableLiveData<>();
    /** UIが書籍詳細データの変更を監視するためのLiveData（公開用） */
    public LiveData<BookDetailData> bookDetail = _bookDetail;

    /** ハイライトメモのリストを保持し、UIに通知するためのMutableLiveData（内部用） */
    private final MutableLiveData<List<HighlightMemoData>> _highlightMemos = new MutableLiveData<>();
    /** UIがハイライトメモのリスト変更を監視するためのLiveData（公開用） */
    public LiveData<List<HighlightMemoData>> highlightMemos = _highlightMemos;

    /**
     * BookDetailViewModelのコンストラクタ。
     * 必要なデータリポジトリのインスタンスを注入します。
     *
     * @param bookRepository データリポジトリのインスタンス
     */
    public BookDetailViewModel(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * 指定された書籍の書籍詳細データをリポジトリから非同期でロードする。
     * ロードが成功した場合、`bookDetail` LiveDataを更新する。
     *
     * @param uid      データをロードするユーザーのID
     * @param volumeId ロード対象の書籍のボリュームID
     */
    public void loadBookDetail(String uid, String volumeId) {
        // 非同期でリポジトリから書籍詳細データを取得
        Future<BookDetailData> future = bookRepository.getBookDetail(uid, volumeId);
        new Thread(() -> {
            try {
                // 非同期処理の完了を待ち、結果を取得
                BookDetailData detailData = future.get();
                // UIスレッドでLiveDataを更新し、UIに通知
                _bookDetail.postValue(detailData);
            } catch (ExecutionException | InterruptedException e) {
                // データロード中のエラーをログに出力
                Log.e(TAG, "Error loading book detail for volumeId: " + volumeId + ", Error: " + e.getMessage());
                // エラー時はLiveDataにnullを設定してUIに通知
                _bookDetail.postValue(null);
            }
        }).start();
    }

    /**
     * 現在の書籍詳細データが存在するかどうかを確認する。
     *
     * @return `bookDetail` LiveDataが非nullの値を保持している場合 `true`、そうでない場合 `false`
     */
    public boolean hasDetail() {
        return _bookDetail.getValue() != null;
    }

    /**
     * 指定された書籍のハイライトメモのリストをリポジトリから非同期でロードする。
     * ロードが成功した場合、`highlightMemos` LiveDataを更新する。
     *
     * @param uid      メモをロードするユーザーのID
     * @param volumeId ロード対象の書籍のボリュームID
     */
    public void loadHighlightMemos(String uid, String volumeId) {
        // 非同期でリポジトリからハイライトメモのリストを取得
        Future<List<HighlightMemoData>> future = bookRepository.getHighlightMemos(uid, volumeId);
        new Thread(() -> {
            try {
                // 非同期処理の完了を待ち、結果を取得
                List<HighlightMemoData> memos = future.get();
                // UIスレッドでLiveDataを更新し、UIに通知
                _highlightMemos.postValue(memos);
            } catch (ExecutionException | InterruptedException e) {
                // メモロード中のエラーをログに出力
                Log.e(TAG, "Error loading highlight memos for volumeId: " + volumeId + ", Error: " + e.getMessage());
                // エラー時はLiveDataにnullを設定してUIに通知
                _highlightMemos.postValue(null);
            }
        }).start();
    }

    /**
     * 新しいハイライトメモをリポジトリに登録する。
     * 登録が成功した場合、`loadHighlightMemos`を呼び出して最新のメモリストを再ロードする。
     *
     * @param uid      メモを登録するユーザーのID
     * @param volumeId メモを登録する書籍のボリュームID
     * @param memoData 登録するHighlightMemoDataオブジェクト
     */
    public void registerHighlightMemo(String uid, String volumeId, HighlightMemoData memoData) {
        // 非同期でリポジトリにハイライトメモを登録
        Future<Boolean> future = bookRepository.registerHighlightMemo(uid, volumeId, memoData);
        new Thread(() -> {
            try {
                // 非同期処理の完了を待ち、登録の成否を取得
                Boolean success = future.get();
                if (success != null && success) {
                    // 登録成功をログに出力し、最新のメモリストを再ロード
                    Log.d(TAG, "Highlight memo registered successfully. Reloading memos.");
                    loadHighlightMemos(uid, volumeId);
                } else {
                    // 登録失敗をログに出力
                    Log.e(TAG, "Failed to register highlight memo. Received success: " + success);
                }
            } catch (ExecutionException | InterruptedException e) {
                // 登録処理中のエラーをログに出力
                Log.e(TAG, "Error registering highlight memo: " + e.getMessage());
            }
        }).start();
    }
}