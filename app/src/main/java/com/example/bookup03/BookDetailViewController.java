package com.example.bookup03;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

public class BookDetailViewController {

    private static final String TAG = "BookDetailViewController";

    public void displayBookDetails(BookDetailData data, View rootView, boolean showNoBookMessage) {
        if (data == null) {
            if (showNoBookMessage) {
                TextView message = rootView.findViewById(R.id.no_book_message);
                if (message != null) message.setVisibility(View.VISIBLE);
            } else {
                Log.w(TAG, "bookDetailDataがnull");
            }
            return;
        }

        TextView titleView = rootView.findViewById(R.id.book_title);
        TextView summaryView = rootView.findViewById(R.id.book_summary);
        TextView statusView = rootView.findViewById(R.id.book_status);
        ImageView coverView = rootView.findViewById(R.id.book_cover);

        if (titleView != null) titleView.setText(data.getName());
        if (summaryView != null) summaryView.setText(data.getSummary());
        if (statusView != null) statusView.setText(data.getPublicStatus());

        if (coverView != null) {
            Glide.with(coverView.getContext()).load(data.getCoverImageUrl()).into(coverView);
        }
    }
}