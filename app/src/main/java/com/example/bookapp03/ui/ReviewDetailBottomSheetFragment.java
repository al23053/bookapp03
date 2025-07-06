package com.example.bookapp03.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.bookapp03.R;
import com.example.bookapp03.model.Review;

import java.util.Locale;

/**
 * 書籍レビューの詳細を表示するためのボトムシートDialogFragmentです。
 * レビューの全文、評価、投稿者名、および投稿日時を表示します。
 */
public class ReviewDetailBottomSheetFragment extends BottomSheetDialogFragment {

    /**
     * レビュー本文をBundleに渡す際のキー。
     */
    private static final String ARG_REVIEW_TEXT = "reviewText";
    /**
     * ユーザー名をBundleに渡す際のキー。
     */
    private static final String ARG_USER_NAME = "userName";

    /**
     * ReviewDetailBottomSheetFragmentの新しいインスタンスを生成するファクトリーメソッドです。
     * 必要なレビュー詳細情報をBundleに格納して返します。
     *
     * @param reviewText レビューの全文
     * @param userName   レビュー投稿者のユーザー名
     * @return 新しいReviewDetailBottomSheetFragmentのインスタンス
     */
    public static ReviewDetailBottomSheetFragment newInstance(String reviewText, String userName) {
        ReviewDetailBottomSheetFragment fragment = new ReviewDetailBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REVIEW_TEXT, reviewText);
        args.putString(ARG_USER_NAME, userName);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * FragmentのUIを生成します。
     * レイアウトをインフレートし、Bundleから渡されたレビュー詳細情報をUI要素に設定します。
     *
     * @param inflater           レイアウトXMLファイルをインフレートするために使用されるLayoutInflater
     * @param container          FragmentのUIがアタッチされる親ViewGroup（もしあれば）
     * @param savedInstanceState 以前に保存された状態データを含むBundleオブジェクト
     * @return Fragmentのルートビュー
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_review_detail, container, false);

        TextView reviewTextView = view.findViewById(R.id.bottom_sheet_review_text);
        TextView userNameTextView = view.findViewById(R.id.bottom_sheet_user_name);

        if (getArguments() != null) {
            String reviewText = getArguments().getString(ARG_REVIEW_TEXT);
            String userName = getArguments().getString(ARG_USER_NAME);

            reviewTextView.setText(reviewText);
            userNameTextView.setText("投稿者: " + userName);

        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog(); // DialogFragmentが管理するDialogを取得

        if (dialog instanceof BottomSheetDialog) {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
            // BottomSheetDialogのレイアウトを取得
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet != null) {
                // BottomSheetBehaviorを取得
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);

                // 画面の高さを取得
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                // ボトムシートの最大高さを画面の約90%に設定
                int desiredHeight = (int) (screenHeight * 0.9); // 例: 画面の90%

                // peekHeight（初期表示の高さ）も desiredHeight に設定すると、常にこの高さで表示される
                behavior.setPeekHeight(desiredHeight);
                // 完全に展開された状態 (Expanded) にする
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                // layoutParams を設定して、BottomSheetの高さ自体も固定する
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.height = desiredHeight;
                    bottomSheet.setLayoutParams(layoutParams);
                }
            }
        }
    }
}
