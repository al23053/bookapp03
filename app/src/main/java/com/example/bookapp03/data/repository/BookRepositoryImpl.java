/**
 * モジュール名: BookRepositoryImpl
 * 作成者: 横山葉
 * 作成日: 2025/06/20
 * 概要: BookRepositoryインターフェースの実装クラス。ローカルDBと外部APIから書籍データを取得・更新する。
 * 履歴:
 * 2025/06/20 横山葉 新規作成
 */
package com.example.bookapp03.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.bookapp03.C6BookInformationManaging.RegisterHighlightMemo;
import com.example.bookapp03.C6BookInformationManaging.RegisterSummary;
import com.example.bookapp03.C6BookInformationManaging.VolumeIdProvider;
import com.example.bookapp03.C6BookInformationManaging.database.BookInformationDatabase;
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoDao;
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoEntity;
import com.example.bookapp03.C6BookInformationManaging.database.SummaryDao;
import com.example.bookapp03.C6BookInformationManaging.database.SummaryEntity;
import com.example.bookapp03.data.model.BookDetailData;
import com.example.bookapp03.data.model.BookSummaryData;
import com.example.bookapp03.data.model.HighlightMemoData;
import com.example.bookapp03.domain.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BookRepositoryImpl implements BookRepository {

    private static final String TAG = "BookRepositoryImpl";
    private final SummaryDao summaryDao;
    private final HighlightMemoDao highlightMemoDao;
    private final RegisterSummary registerSummary;
    private final RegisterHighlightMemo registerHighlightMemo;
    private final ExecutorService executor; // 非同期処理のためのExecutorService

    public BookRepositoryImpl(Context context) {
        BookInformationDatabase db = BookInformationDatabase.getDatabase(context);
        this.summaryDao = db.summaryDao();
        this.highlightMemoDao = db.highlightMemoDao();
        this.registerSummary = new RegisterSummary(context); // RegisterSummaryもContextが必要
        this.registerHighlightMemo = new RegisterHighlightMemo(context); // RegisterHighlightMemoもContextが必要
        this.executor = Executors.newFixedThreadPool(4); // 適切なスレッドプールサイズを設定
    }

    @Override
    public Future<List<BookSummaryData>> getAllBookSummaries(String uid) {
        return executor.submit(() -> {
            List<BookSummaryData> bookSummaries = new ArrayList<>();
            try {
                // RoomからSummaryEntityを全て取得
                List<SummaryEntity> summaryEntities = summaryDao.getAllSummariesByUser(uid);

                for (SummaryEntity entity : summaryEntities) {
                    // VolumeIdProviderを使って書籍名と画像URLを取得（ネットワークアクセス）
                    // ここは同期的に待つが、ViewModelでの呼び出し元で非同期化されるため問題なし
                    String title = VolumeIdProvider.fetchBookName(entity.volumeId);
                    String imageUrl = VolumeIdProvider.fetchCoverImageUrl(entity.volumeId);

                    BookSummaryData summaryData = new BookSummaryData(entity.volumeId, title, imageUrl);
                    summaryData.setPublic(entity.isPublic); // 公開ステータスも設定
                    bookSummaries.add(summaryData);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching all book summaries: " + e.getMessage());
            }
            return bookSummaries;
        });
    }

    @Override
    public Future<BookDetailData> getBookDetail(String uid, String volumeId) {
        return executor.submit(() -> {
            try {
                // RoomからSummaryEntityを取得
                SummaryEntity summaryEntity = summaryDao.getSummary(uid, volumeId);

                if (summaryEntity != null) {
                    // VolumeIdProviderを使って書籍名と画像URLを取得（ネットワークアクセス）
                    String name = VolumeIdProvider.fetchBookName(volumeId);
                    String coverImageUrl = VolumeIdProvider.fetchCoverImageUrl(volumeId);

                    return new BookDetailData(
                            summaryEntity.volumeId,
                            name,
                            summaryEntity.overallSummary,
                            coverImageUrl,
                            summaryEntity.isPublic ? "public" : "private"
                    );
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching book detail for volumeId: " + volumeId + ", Error: " + e.getMessage());
            }
            return null;
        });
    }

    @Override
    public Future<Boolean> updateBookPublicStatus(String uid, String volumeId, boolean isPublic) {
        return executor.submit(() -> {
            try {
                // 現在の要約テキストを取得してから更新する必要がある
                SummaryEntity existingSummary = summaryDao.getSummary(uid, volumeId);
                if (existingSummary != null) {
                    // RegisterSummaryは内部でFirestoreとRoomを更新する
                    return registerSummary.registerSummary(uid, volumeId, existingSummary.overallSummary, isPublic);
                }
                return false;
            } catch (Exception e) {
                Log.e(TAG, "Error updating book public status for volumeId: " + volumeId + ", Error: " + e.getMessage());
                return false;
            }
        });
    }

    @Override
    public Future<List<HighlightMemoData>> getHighlightMemos(String uid, String volumeId) {
        return executor.submit(() -> {
            List<HighlightMemoData> memoList = new ArrayList<>();
            try {
                List<HighlightMemoEntity> entities = highlightMemoDao.getByUserAndVolume(uid, volumeId);
                for (HighlightMemoEntity entity : entities) {
                    memoList.add(new HighlightMemoData(
                            String.valueOf(entity.page), // intからStringへの変換
                            String.valueOf(entity.line), // intからStringへの変換
                            entity.memo
                    ));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching highlight memos for volumeId: " + volumeId + ", Error: " + e.getMessage());
            }
            return memoList;
        });
    }

    @Override
    public Future<Boolean> registerHighlightMemo(String uid, String volumeId, HighlightMemoData memoData) {
        return executor.submit(() -> {
            try {
                return registerHighlightMemo.registerHighlightMemo(uid, volumeId, memoData);
            } catch (Exception e) {
                Log.e(TAG, "Error registering highlight memo for volumeId: " + volumeId + ", Error: " + e.getMessage());
                return false;
            }
        });
    }
}