/**
 * モジュール名: 本の詳細表示
 * 作成者: 横山葉
 * 作成日: 2025/06/20
 * 概要: 書籍の詳細情報を表示し、ハイライトメモの管理を行うアクティビティ。
 * 履歴:
 * 2025/06/20 横山葉 新規作成
 */
package com.example.bookapp03.C1UIProcessing;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.util.Log;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import com.example.bookapp03.R;
import com.example.bookapp03.C3BookInformationProcessing.BookDetailViewModel;
import com.example.bookapp03.C5UserInformationManaging.UserAuthManager;

import java.util.Collections;

public class BookDetailActivity extends AppCompatActivity {

    private BookDetailViewController controller;
    private BookDetailViewModel viewModel;
    private HighlightMemoBottomSheetController highlightMemoBottomSheetController;
    private String currentVolumeId;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        ImageButton backButton = findViewById(R.id.back_button);
        if (backButton != null) {
            Log.d("BookDetailActivity", "Back button found. Setting OnClickListener."); // ボタンが見つかったことをログで確認
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("BookDetailActivity", "Back button clicked. Calling onBackPressed()."); // ボタンがクリックされたことをログで確認
                    onBackPressed();
                }
            });
        } else {
            Log.e("BookDetailActivity", "R.id.back_button が見つかりません。XMLを確認してください。");
        }

        controller = new BookDetailViewController();
        View rootView = findViewById(android.R.id.content);

        viewModel = new ViewModelProvider(this, new ViewModelFactory(getApplicationContext()))
                .get(BookDetailViewModel.class);

        highlightMemoBottomSheetController = new HighlightMemoBottomSheetController();

        LinearLayout bottomSheet = rootView.findViewById(R.id.bottom_sheet_memo_container);
        if (bottomSheet != null) {
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

            bottomSheet.post(() -> {
                bottomSheetBehavior.setPeekHeight(120, true);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                Log.d("BottomSheet", "post後の状態設定: " + bottomSheetBehavior.getState());
            });

            bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    Log.d("BottomSheet", "State changed: " + newState);
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    // スライド中の処理（必要ならここに記述）
                }
            });

            Log.d("BookDetailActivity", "Highlight memo bottom sheet container successfully initialized with Behavior.");
        } else {
            Log.e("BookDetailActivity", "R.id.bottom_sheet_memo_container が見つかりません。XMLを確認してください。");
        }

        currentVolumeId = getIntent().getStringExtra("volumeId");
        String currentUserId = UserAuthManager.getCurrentUid();

        viewModel.bookDetail.observe(this, bookDetailData -> {
            boolean showNoBookMessage = (currentVolumeId == null || currentVolumeId.isEmpty()) || (bookDetailData == null);
            controller.displayBookDetails(bookDetailData, rootView, showNoBookMessage);

            if (bookDetailData != null && bookDetailData.getVolumeId() != null) {
                if (currentUserId != null) {
                    viewModel.loadHighlightMemos(currentUserId, bookDetailData.getVolumeId());
                } else {
                    Log.e("BookDetailActivity", "User not logged in or UID not available for loading highlight memos.");
                }
            }
        });

        viewModel.highlightMemos.observe(this, memos -> {
            if (memos != null && !memos.isEmpty()) {
                highlightMemoBottomSheetController.displayMemo(rootView, memos);
            } else {
                highlightMemoBottomSheetController.displayMemo(rootView, Collections.emptyList());
            }
        });

        if (currentUserId != null && currentVolumeId != null && !currentVolumeId.isEmpty()) {
            viewModel.loadBookDetail(currentUserId, currentVolumeId);
        } else {
            controller.displayBookDetails(null, rootView, true);
            if (currentUserId == null) {
                Log.e("BookDetailActivity", "User not logged in or UID not available for loading book details.");
            }
            if (currentVolumeId == null || currentVolumeId.isEmpty()) {
                Log.e("BookDetailActivity", "Volume ID is null or empty. Cannot load book details.");
            }
        }
    }
}