package com.example.bookup03;

import android.util.Log;

public class PublicPrivateToggleHandler {

    private final BookListViewModel viewModel;

    public PublicPrivateToggleHandler(BookListViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void handleToggle(String bookId, boolean newPublicStatus) {
        if (viewModel == null || bookId == null) {
            Log.w("ToggleHandler", "ViewModel または bookId が null");
            return;
        }

        // ViewModelに状態変更を依頼
        viewModel.updatePublicStatus(bookId, newPublicStatus);

        // ログ出力（確認用）
        Log.d("ToggleHandler", "本ID: " + bookId + " を " + (newPublicStatus ? "公開" : "非公開") + " に更新");
    }
}