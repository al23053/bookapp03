package com.example.bookup03.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookup03.logic.BookListViewController;
import com.example.bookup03.data.BookListViewModel;
import com.example.bookup03.data.BookSummaryData;
import com.example.bookup03.R;

import java.util.ArrayList;
import java.util.List;

public class BookListActivity extends AppCompatActivity {

    private BookListViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new BookListViewModel();

        List<BookSummaryData> dummyBooks = new ArrayList<>();
        dummyBooks.add(new BookSummaryData("1", "本A", "https://example.com/sample1.jpg"));
        dummyBooks.add(new BookSummaryData("2", "本B", "https://example.com/sample2.jpg"));
        dummyBooks.add(new BookSummaryData("3", "本C", "https://example.com/sample3.jpg"));

        for (BookSummaryData book : dummyBooks) {
            book.setPublic(false);
        }

        viewModel.setBooks(dummyBooks);

        RecyclerView recyclerView = findViewById(R.id.book_list_recycler);
        new BookListViewController().displayBookList(recyclerView, dummyBooks, viewModel, dummyBooks.isEmpty());
    }
}