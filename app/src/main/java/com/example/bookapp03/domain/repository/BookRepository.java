/**
 * モジュール名: BookRepository
 * 作成者: 横山葉
 * 作成日: 2025/06/20
 * 概要: アプリケーションの書籍データ操作を抽象化するインターフェース
 * 履歴:
 * 2025/06/20 横山葉 新規作成
 */
package com.example.bookapp03.domain.repository;

import com.example.bookapp03.data.model.BookDetailData;
import com.example.bookapp03.data.model.BookSummaryData;
import com.example.bookapp03.data.model.HighlightMemoData;

import java.util.List;
import java.util.concurrent.Future;

public interface BookRepository {

    /**
     * 指定されたユーザーIDの全ての書籍サマリを取得する。
     * @param uid ユーザーID
     * @return 書籍サマリのリスト
     */
    Future<List<BookSummaryData>> getAllBookSummaries(String uid);

    /**
     * 指定されたユーザーIDとボリュームIDの書籍詳細情報を取得する。
     * @param uid ユーザーID
     * @param volumeId ボリュームID
     * @return 書籍詳細データ。見つからない場合はnull
     */
    Future<BookDetailData> getBookDetail(String uid, String volumeId);

    /**
     * 指定された書籍の公開ステータスを更新する。
     * @param uid ユーザーID
     * @param volumeId ボリュームID
     * @param isPublic 新しい公開ステータス
     * @return 更新が成功した場合はtrue、それ以外はfalse
     */
    Future<Boolean> updateBookPublicStatus(String uid, String volumeId, boolean isPublic);

    /**
     * 指定されたユーザーIDとボリュームIDのハイライトメモを取得する。
     * @param uid ユーザーID
     * @param volumeId ボリュームID
     * @return ハイライトメモのリスト
     */
    Future<List<HighlightMemoData>> getHighlightMemos(String uid, String volumeId);

    /**
     * ハイライトメモを登録する。
     * @param uid ユーザーID
     * @param volumeId ボリュームID
     * @param memoData ハイライトメモデータ
     * @return 登録が成功した場合はtrue、それ以外はfalse
     */
    Future<Boolean> registerHighlightMemo(String uid, String volumeId, HighlightMemoData memoData);
}