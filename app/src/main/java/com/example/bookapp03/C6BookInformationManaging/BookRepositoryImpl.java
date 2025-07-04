/**
 * モジュール名: 本データ取得インタフェース
 * 作成者: 横山葉
 * 作成日: 2025/06/20
 * 概要: BookRepositoryインターフェースの実装クラス。ローカルDB (Room) と外部API (VolumeIdProvider) から
 * 書籍データおよびハイライトメモを取得・更新する責任を持つ。
 * 履歴:
 * 2025/06/20 横山葉 新規作成
 */
package com.example.bookapp03.C6BookInformationManaging;

import android.content.Context;
import android.util.Log;

import com.example.bookapp03.C6BookInformationManaging.database.BookInformationDatabase;
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoDao;
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoEntity;
import com.example.bookapp03.C6BookInformationManaging.database.SummaryDao;
import com.example.bookapp03.C6BookInformationManaging.database.SummaryEntity;
import com.example.bookapp03.C3BookInformationProcessing.BookDetailData;
import com.example.bookapp03.C3BookInformationProcessing.BookSummaryData;
import com.example.bookapp03.C1UIProcessing.HighlightMemoData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 書籍データとハイライトメモの取得および更新を行うリポジトリの実装クラス。
 * データベース（Room）とネットワーク操作を抽象化し、ViewModelからのデータ要求に応答します。
 */
public class BookRepositoryImpl implements BookRepository {

    /** ログ出力用のタグ */
    private static final String TAG = "BookRepositoryImpl";
    /** 書籍サマリのデータアクセスオブジェクト (DAO) */
    private final SummaryDao summaryDao;
    /** ハイライトメモのデータアクセスオブジェクト (DAO) */
    private final HighlightMemoDao highlightMemoDao;
    /** 書籍サマリ登録のビジネスロジックを扱うクラス */
    private final RegisterSummary registerSummary;
    /** ハイライトメモ登録のビジネスロジックを扱うクラス */
    private final RegisterHighlightMemo registerHighlightMemo;
    /** 非同期処理を実行するためのスレッドプール */
    private final ExecutorService executor;

    /**
     * BookRepositoryImplのコンストラクタ。
     * データベースDAOとビジネスロジッククラスを初期化します。
     *
     * @param context アプリケーションコンテキスト
     */
    public BookRepositoryImpl(Context context) {
        // Roomデータベースのインスタンスを取得
        BookInformationDatabase db = BookInformationDatabase.getDatabase(context);
        this.summaryDao = db.summaryDao();
        this.highlightMemoDao = db.highlightMemoDao();
        this.registerSummary = new RegisterSummary(context);
        this.registerHighlightMemo = new RegisterHighlightMemo(context);
        // 固定数のスレッドを持つExecutorServiceを初期化
        this.executor = Executors.newFixedThreadPool(4);
    }

    /**
     * 指定されたユーザーの全ての書籍サマリを非同期で取得します。
     * ローカルDBからサマリエンティティを取得し、必要に応じてネットワークから書籍名と画像URLをフェッチします。
     *
     * @param uid ユーザーID
     * @return 書籍サマリのリストを含むFutureオブジェクト
     */
    @Override
    public Future<List<BookSummaryData>> getAllBookSummaries(String uid) {
        return executor.submit(() -> {
            List<BookSummaryData> bookSummaries = new ArrayList<>();
            try {
                // Roomから指定ユーザーのSummaryEntityを全て取得
                List<SummaryEntity> summaryEntities = summaryDao.getAllSummariesByUser(uid);

                // 各SummaryEntityからBookSummaryDataを構築
                for (SummaryEntity entity : summaryEntities) {
                    // VolumeIdProviderを使って書籍名と画像URLを取得（ネットワークアクセス）
                    // この部分は同期的に完了を待つが、このメソッド自体がFutureでラップされているため呼び出し元は非同期で処理可能
                    String title = VolumeIdProvider.fetchBookName(entity.volumeId);
                    String imageUrl = VolumeIdProvider.fetchCoverImageUrl(entity.volumeId);

                    // BookSummaryDataインスタンスを作成し、公開ステータスを設定
                    BookSummaryData summaryData = new BookSummaryData(entity.volumeId, title, imageUrl);
                    summaryData.setPublic(entity.isPublic);
                    bookSummaries.add(summaryData);
                }
            } catch (Exception e) {
                // エラー発生時はログに出力
                Log.e(TAG, "Error fetching all book summaries for UID: " + uid + ", Error: " + e.getMessage());
                // エラー発生時は空のリストを返すことも検討するが、ここではcatchして処理を続ける
            }
            return bookSummaries;
        });
    }

    /**
     * 指定されたユーザーとボリュームIDの書籍詳細データを非同期で取得します。
     * ローカルDBから要約エンティティを取得し、必要に応じてネットワークから書籍名と画像URLをフェッチします。
     *
     * @param uid      ユーザーID
     * @param volumeId 書籍のボリュームID
     * @return 書籍詳細データを含むFutureオブジェクト。データが見つからない場合やエラーの場合はnullを含むFutureを返す。
     */
    @Override
    public Future<BookDetailData> getBookDetail(String uid, String volumeId) {
        return executor.submit(() -> {
            try {
                // Roomから指定ユーザーとボリュームIDのSummaryEntityを取得
                SummaryEntity summaryEntity = summaryDao.getSummary(uid, volumeId);

                // SummaryEntityが存在する場合、BookDetailDataを構築
                if (summaryEntity != null) {
                    // VolumeIdProviderを使って書籍名と画像URLを取得（ネットワークアクセス）
                    String name = VolumeIdProvider.fetchBookName(volumeId);
                    String coverImageUrl = VolumeIdProvider.fetchCoverImageUrl(volumeId);

                    // BookDetailDataインスタンスを作成して返す
                    return new BookDetailData(
                            summaryEntity.volumeId,
                            name,
                            summaryEntity.overallSummary,
                            coverImageUrl,
                            summaryEntity.isPublic ? "public" : "private" // isPublicに応じてステータス文字列を設定
                    );
                }
            } catch (Exception e) {
                // エラー発生時はログに出力
                Log.e(TAG, "Error fetching book detail for volumeId: " + volumeId + ", Error: " + e.getMessage());
            }
            return null; // データが見つからないかエラーの場合はnullを返す
        });
    }

    /**
     * 指定された書籍の公開ステータスを非同期で更新します。
     * ローカルDBから既存の要約情報を取得し、その全体要約を維持したまま公開ステータスを更新します。
     *
     * @param uid          ユーザーID
     * @param volumeId     対象の書籍のボリュームID
     * @param newPublicStatus 新しい公開状態（true: 公開, false: 非公開）
     * @return 更新が成功した場合はtrue、失敗した場合はfalseを含むFutureオブジェクト
     */
    @Override
    public Future<Boolean> updateBookPublicStatus(String uid, String volumeId, boolean newPublicStatus) {
        return executor.submit(() -> {
            try {
                // 公開ステータスを更新するために、現在の要約テキストを取得する必要がある
                SummaryEntity existingSummary = summaryDao.getSummary(uid, volumeId);
                if (existingSummary != null) {
                    // RegisterSummaryクラスを介してFirestoreとRoomの両方を更新
                    return registerSummary.registerSummary(uid, volumeId, existingSummary.overallSummary, newPublicStatus);
                }
                Log.w(TAG, "No existing summary found for volumeId: " + volumeId + " to update public status.");
                return false;
            } catch (Exception e) {
                // エラー発生時はログに出力
                Log.e(TAG, "Error updating book public status for volumeId: " + volumeId + ", Error: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * 指定されたユーザーとボリュームIDのハイライトメモのリストを非同期で取得します。
     * ローカルDBからハイライトメモエンティティを取得し、データクラスに変換します。
     *
     * @param uid      ユーザーID
     * @param volumeId 書籍のボリュームID
     * @return ハイライトメモのリストを含むFutureオブジェクト。エラーの場合は空のリストを返す。
     */
    @Override
    public Future<List<HighlightMemoData>> getHighlightMemos(String uid, String volumeId) {
        return executor.submit(() -> {
            List<HighlightMemoData> memoList = new ArrayList<>();
            try {
                // Roomから指定ユーザーとボリュームIDのHighlightMemoEntityを取得
                List<HighlightMemoEntity> entities = highlightMemoDao.getByUserAndVolume(uid, volumeId);
                // 各エンティティをHighlightMemoDataに変換
                for (HighlightMemoEntity entity : entities) {
                    memoList.add(new HighlightMemoData(
                            entity.page,
                            entity.line,
                            entity.memo
                    ));
                }
            } catch (Exception e) {
                // エラー発生時はログに出力
                Log.e(TAG, "Error fetching highlight memos for volumeId: " + volumeId + ", Error: " + e.getMessage());
            }
            return memoList;
        });
    }

    /**
     * 指定されたユーザーとボリュームIDに対してハイライトメモを非同期で登録します。
     *
     * @param uid      メモを登録するユーザーID
     * @param volumeId メモを登録する書籍のボリュームID
     * @param memoData 登録するHighlightMemoDataオブジェクト
     * @return 登録が成功した場合はtrue、失敗した場合はfalseを含むFutureオブジェクト
     */
    @Override
    public Future<Boolean> registerHighlightMemo(String uid, String volumeId, HighlightMemoData memoData) {
        return executor.submit(() -> {
            try {
                // RegisterHighlightMemoクラスを介してメモを登録
                return registerHighlightMemo.registerHighlightMemo(uid, volumeId, memoData);
            } catch (Exception e) {
                // エラー発生時はログに出力
                Log.e(TAG, "Error registering highlight memo for volumeId: " + volumeId + ", Error: " + e.getMessage());
                return false;
            }
        });
    }
}