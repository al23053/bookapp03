package com.example.bookup03;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BookListViewController {

    private static final String TAG = "BookListViewController";

    public void displayBookList(RecyclerView recyclerView, List<BookSummaryData> bookSummaries,
                                BookListViewModel viewModel, boolean showEmptyMessage) {
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerViewがnull");
            return;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        BookListAdapter adapter = new BookListAdapter(bookSummaries, viewModel); // ← 修正
        recyclerView.setAdapter(adapter);

        View parentView = (View) recyclerView.getParent();
        TextView emptyTextView = parentView.findViewById(R.id.empty_text);

        if (emptyTextView != null) {
            emptyTextView.setVisibility(showEmptyMessage ? View.VISIBLE : View.GONE);
        }
    }
}