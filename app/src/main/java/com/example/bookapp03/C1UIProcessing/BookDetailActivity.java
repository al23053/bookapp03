package com.example.bookapp03.C1UIProcessing;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import java.util.List;
import java.lang.StringBuilder;
import android.util.Log;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import com.example.bookapp03.R;
import com.example.bookapp03.C3BookInformationProcessing.BookDetailViewModel;
import com.example.bookapp03.C5UserInformationManaging.UserAuthManager;
import com.example.bookapp03.C1UIProcessing.BookDetailViewController;
import com.example.bookapp03.C1UIProcessing.ViewModelFactory;
import com.example.bookapp03.C1UIProcessing.HighlightMemoData;
import com.example.bookapp03.C1UIProcessing.HighlightMemoBottomSheetController;

import android.widget.LinearLayout;



/**
 * 書籍詳細画面を表示するアクティビティ
 */
public class BookDetailActivity extends AppCompatActivity {

    private BookDetailViewController controller;
    private BookDetailViewModel viewModel;
    private HighlightMemoBottomSheetController highlightMemoBottomSheetController;
    private String currentVolumeId; // 現在表示している書籍のvolumeIdを保持

    // ★追加: BottomSheetBehaviorのインスタンス★
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        controller = new BookDetailViewController();
        View rootView = findViewById(android.R.id.content); // Activityのルートビューを取得

        // ViewModelProviderを使用してViewModelを初期化
        viewModel = new ViewModelProvider(this, new ViewModelFactory(getApplicationContext()))
                .get(BookDetailViewModel.class);

        // HighlightMemoBottomSheetControllerを初期化
        highlightMemoBottomSheetController = new HighlightMemoBottomSheetController();

        // ★修正点1: ボトムシートのコンテナビューを取得し、BottomSheetBehaviorを設定★
        LinearLayout bottomSheet = rootView.findViewById(R.id.bottom_sheet_memo_container);
        if (bottomSheet != null) {
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            // XMLで app:behavior_peekHeight="120dp" と app:behavior_hideable="false" を設定しているので、
            // ボトムシートは初期状態で常に表示されます。
            // したがって、以下のような明示的なsetVisibility(View.VISIBLE)は不要です。
            // bottomSheet.setVisibility(View.VISIBLE); // これも不要

            // 必要であれば、ボトムシートの状態変化を監視することもできます
            bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    // ここで状態変化に応じた処理を記述できます (例: EXPANDED, COLLAPSED, DRAGGINGなど)
                    Log.d("BottomSheet", "State changed: " + newState);
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    // スライド中の処理
                }
            });
            Log.d("BookDetailActivity", "Highlight memo bottom sheet container successfully initialized with Behavior.");
        } else {
            Log.e("BookDetailActivity", "R.id.bottom_sheet_memo_container が見つかりません。XMLを確認してください。");
        }


        // インテントからvolumeIdを取得
        currentVolumeId = getIntent().getStringExtra("volumeId");

        // 実際のユーザーUIDを取得
        String currentUserId = UserAuthManager.getCurrentUid();

        // LiveDataの変更を監視: 書籍詳細データ
        viewModel.bookDetail.observe(this, bookDetailData -> {
            // データが更新されたらUIを更新
            boolean showNoBookMessage = (currentVolumeId == null || currentVolumeId.isEmpty()) || (bookDetailData == null);
            controller.displayBookDetails(bookDetailData, rootView, showNoBookMessage);

            // ハイライトメモもロード
            if (bookDetailData != null && bookDetailData.getVolumeId() != null) {
                if (currentUserId != null) {
                    viewModel.loadHighlightMemos(currentUserId, bookDetailData.getVolumeId());
                } else {
                    Log.e("BookDetailActivity", "User not logged in or UID not available for loading highlight memos.");
                }
            }
        });

        // LiveDataの変更を監視: ハイライトメモ
        // ハイライトメモが更新されたら、その内容をボトムシートのTextViewに表示します。
        viewModel.highlightMemos.observe(this, memos -> {
            if (memos != null && !memos.isEmpty()) {
                StringBuilder combinedMemos = new StringBuilder();
                for (HighlightMemoData memo : memos) {
                    combinedMemos.append("ページ: ").append(memo.getPage())
                            .append(", 行: ").append(memo.getLine())
                            .append("\nメモ: ").append(memo.getMemo())
                            .append("\n\n");
                }
                // ハイライトメモのテキストを更新
                highlightMemoBottomSheetController.displayMemo(rootView, combinedMemos.toString().trim());
                // もし必要なら、ここでボトムシートを展開状態にする
                // bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                // メモがない場合はその旨を表示
                highlightMemoBottomSheetController.displayMemo(rootView, "ハイライトメモはありません。");
                // メモがない場合でも、ボトムシートはpeekHeightで表示され続けます
            }
        });

        // ★削除: ここでボトムシートの可視性を制御する必要はありません。XMLとBottomSheetBehaviorが自動的に管理します。★
        // View memoContainer = rootView.findViewById(R.id.highlight_memo_text);
        // if (memoContainer != null) {
        //     memoContainer.setVisibility(View.VISIBLE);
        //     Log.d("BookDetailActivity", "Highlight memo bottom sheet container made visible.");
        // } else {
        //     Log.e("BookDetailActivity", "bottom_sheet_memo_container (仮のボトムシートコンテナ) が見つかりません。");
        // }


        // Activityが作成されたらViewModelにデータのロードを指示
        if (currentUserId != null && currentVolumeId != null && !currentVolumeId.isEmpty()) {
            viewModel.loadBookDetail(currentUserId, currentVolumeId);
        } else {
            // UIDまたはvolumeIdがない場合は「該当書籍なし」メッセージを表示
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