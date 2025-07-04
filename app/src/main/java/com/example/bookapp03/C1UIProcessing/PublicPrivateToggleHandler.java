/**
 * モジュール名: 公開・非公開の切替を受付, 処理部へ流す処理
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 本の公開・非公開状態の切り替え操作を受け付け、ViewModelに伝えるハンドラークラス。
 * 履歴:
 * 2025/06/14 横山葉 新規作成
 */
package com.example.bookapp03.C1UIProcessing;

import android.util.Log;

import com.example.bookapp03.C3BookInformationProcessing.BookListViewModel;

/**
 * 書籍の公開・非公開切り替えの操作を処理し、ViewModelにその変更を伝えるクラス。
 * UI層とビジネスロジック層の間の橋渡しを行います。
 */
public class PublicPrivateToggleHandler {

    /** 書籍リストデータを管理するViewModelのインスタンス */
    private final BookListViewModel viewModel;

    /**
     * PublicPrivateToggleHandlerのコンストラクタ。
     * 依存するBookListViewModelのインスタンスを受け取ります。
     *
     * @param viewModel 書籍リストデータを管理するBookListViewModelのインスタンス。
     */
    public PublicPrivateToggleHandler(BookListViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * 書籍の公開状態切り替え操作を受け付け、ViewModelに更新を指示する。
     * ViewModel、ユーザーID、または書籍IDがnullの場合は処理を行わず警告ログを出力する。
     *
     * @param uid             操作を行ったユーザーのID。
     * @param volumeId        状態を切り替える対象の書籍ボリュームID。
     * @param newPublicStatus 切り替え後の新しい公開状態（true: 公開, false: 非公開）。
     */
    public void handleToggle(String uid, String volumeId, boolean newPublicStatus) {
        // 必須パラメータのいずれかがnullの場合、警告ログを出力し処理を中断
        if (viewModel == null || uid == null || volumeId == null) {
            Log.w("PublicPrivateToggleHandler", "ViewModel, UID, または VolumeId がnullのため、公開状態の更新をスキップしました。");
            return;
        }

        // ViewModelのupdatePublicStatusメソッドを呼び出し、公開状態の更新を依頼
        viewModel.updatePublicStatus(uid, volumeId, newPublicStatus);
    }
}