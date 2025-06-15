/**
 * モジュール名: 公開・非公開の切替を受付,  処理部へ流す処理
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 本の公開・非公開状態の切り替えをViewModelに伝えるハンドラクラス
 * 履歴:
 * 2025/06/14 横山葉 新規作成
 */

package com.example.bookup03;

import android.util.Log;

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
     * @param bookId 対象の本のID
     * @param newPublicStatus 新しい公開状態
     */
    public void handleToggle(String bookId, boolean newPublicStatus) {
        if (viewModel == null || bookId == null) {
            Log.w("ToggleHandler", "ViewModel または bookId が null");
            return;
        }

        viewModel.updatePublicStatus(bookId, newPublicStatus);

        Log.d("ToggleHandler", "本ID: " + bookId + " を " + (newPublicStatus ? "公開" : "非公開") + " に更新");
    }
}