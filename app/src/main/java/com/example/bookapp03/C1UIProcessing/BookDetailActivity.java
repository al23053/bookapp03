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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import com.example.bookapp03.R;
import com.example.bookapp03.C3BookInformationProcessing.BookDetailViewModel;
import com.example.bookapp03.C5UserInformationManaging.UserAuthManager;

import java.util.Collections;

/**
 * 書籍詳細画面を表示するアクティビティ
 */
public class BookDetailActivity extends AppCompatActivity {

    /** 書籍詳細画面のUI操作を管理するコントローラー */
    private BookDetailViewController controller;
    /** 書籍詳細データとハイライトメモデータを管理するViewModel */
    private BookDetailViewModel viewModel;
    /** ハイライトメモ表示用ボトムシートのUI操作を管理するコントローラー */
    private HighlightMemoBottomSheetController highlightMemoBottomSheetController;
    /** 現在表示している書籍のGoogle Books APIのvolumeIdを保持 */
    private String currentVolumeId;
    /** ハイライトメモ表示用ボトムシートの挙動を制御するBehavior */
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;

    /**
     * アクティビティが最初に作成されたときに呼び出される。
     * UIの初期設定、ViewModelの初期化、データ監視、イベントハンドラの設定を行う。
     *
     * @param savedInstanceState アクティビティの以前の保存状態を含むBundleオブジェクト。
     * アクティビティが以前に存在し、最後に終了していなかった場合に非nullとなる。
     */
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

        // ボトムシートのコンテナビューを取得し、BottomSheetBehaviorを設定
        LinearLayout bottomSheet = rootView.findViewById(R.id.bottom_sheet_memo_container);
        if (bottomSheet != null) {
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

            // 明示的にpeekHeightを設定し、COLLAPSED状態にする
            bottomSheet.post(() -> {
                bottomSheetBehavior.setPeekHeight(120, true);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                Log.d("BottomSheet", "post後の状態設定: " + bottomSheetBehavior.getState());
            });

            // ボトムシートの状態変更コールバックを設定
            bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                /**
                 * ボトムシートの状態が変更されたときに呼び出される。
                 * @param bottomSheet 状態が変更されたボトムシートビュー
                 * @param newState 新しい状態（STATE_COLLAPSED, STATE_EXPANDEDなど）
                 */
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    Log.d("BottomSheet", "State changed: " + newState);
                }

                /**
                 * ボトムシートがスライドしているときに呼び出される。
                 * @param bottomSheet スライドしているボトムシートビュー
                 * @param slideOffset スライドオフセット（0から1の範囲）
                 */
                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    // スライド中の処理（必要ならここに記述）
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
                highlightMemoBottomSheetController.displayMemo(rootView, memos);
            } else {
                highlightMemoBottomSheetController.displayMemo(rootView, Collections.emptyList());
            }
        });


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