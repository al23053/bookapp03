package com.example.bookapp03.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.bookapp03.R;
import com.example.bookapp03.model.Review;

import java.text.SimpleDateFormat;
import java.util.Date;
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
     * 評価をBundleに渡す際のキー。
     */
    private static final String ARG_RATING = "rating";
    /**
     * ユーザー名をBundleに渡す際のキー。
     */
    private static final String ARG_USER_NAME = "userName";
    /**
     * タイムスタンプをBundleに渡す際のキー。
     */
    private static final String ARG_TIMESTAMP = "timestamp";

    /**
     * ReviewDetailBottomSheetFragmentの新しいインスタンスを生成するファクトリーメソッドです。
     * 必要なレビュー詳細情報をBundleに格納して返します。
     *
     * @param reviewText レビューの全文
     * @param rating     レビューの評価
     * @param userName   レビュー投稿者のユーザー名
     * @param timestamp  レビューのタイムスタンプ（ミリ秒単位のlong型）
     * @return 新しいReviewDetailBottomSheetFragmentのインスタンス
     */
    public static ReviewDetailBottomSheetFragment newInstance(String reviewText, double rating, String userName, long timestamp) {
        ReviewDetailBottomSheetFragment fragment = new ReviewDetailBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REVIEW_TEXT, reviewText);
        args.putDouble(ARG_RATING, rating);
        args.putString(ARG_USER_NAME, userName);
        args.putLong(ARG_TIMESTAMP, timestamp);
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
        TextView ratingTextView = view.findViewById(R.id.bottom_sheet_rating);
        TextView userNameTextView = view.findViewById(R.id.bottom_sheet_user_name);
        TextView timestampTextView = view.findViewById(R.id.bottom_sheet_timestamp);

        if (getArguments() != null) {
            String reviewText = getArguments().getString(ARG_REVIEW_TEXT);
            double rating = getArguments().getDouble(ARG_RATING);
            String userName = getArguments().getString(ARG_USER_NAME);
            long timestamp = getArguments().getLong(ARG_TIMESTAMP);

            reviewTextView.setText(reviewText);
            ratingTextView.setText("評価: " + String.format("%.1f", rating));
            userNameTextView.setText("投稿者: " + userName);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN);
            String formattedDate = sdf.format(new Date(timestamp));
            timestampTextView.setText("投稿日時: " + formattedDate);
        }

        return view;
    }
}
