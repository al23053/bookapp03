/**
 * モジュール名: 公開・非公開の切替を受付,  処理部へ流す処理
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 本の公開・非公開状態の切り替えをViewModelに伝えるハンドラクラス
 * 履歴:
 * 2025/06/14 横山葉 新規作成
 */

package com.example.bookapp03.C1UIProcessing;

import android.util.Log;

import com.example.bookapp03.C3BookInformationProcessing.BookListViewModel; // ViewModelのパッケージ変更

/**
 * 公開・非公開切り替えの操作を扱うクラス
 */
public class PublicPrivateToggleHandler {

    private final BookListViewModel viewModel;

    /**
     * コンストラクタ
     * @param viewModel BookListViewModelのインスタンス
     */
    public PublicPrivateToggleHandler(BookListViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * 公開状態の切り替えをViewModelに伝える
     * @param uid ユーザーID
     * @param volumeId 対象の本のID
     * @param newPublicStatus 新しい公開状態
     */
    public void handleToggle(String uid, String volumeId, boolean newPublicStatus) {
        if (viewModel == null || uid == null || volumeId == null) {
            Log.w("ToggleHandler", "ViewModel, UID, または VolumeId が null");
            return;
        }

        viewModel.updatePublicStatus(uid, volumeId, newPublicStatus);
    }
}