package com.example.bookup03;

import java.util.ArrayList;
import java.util.List;

public class BookListViewModel {

    private List<BookSummaryData> bookList = new ArrayList<>();

    public List<BookSummaryData> getBooks() {
        return bookList;
    }

    public void setBooks(List<BookSummaryData> books) {
        this.bookList = books;
    }

    public boolean isEmpty() {
        return bookList == null || bookList.isEmpty();
    }

    // 公開・非公開切り替え処理
    public void updatePublicStatus(String bookId, boolean isPublic) {
        for (BookSummaryData book : bookList) {
            if (book.getVolumeId().equals(bookId)) {
                book.setPublic(isPublic);
                // ここでDBや通信処理を入れる予定があれば追加
                System.out.println("公開状態更新: " + book.getTitle() + " → " + (isPublic ? "公開" : "非公開"));
                break;
            }
        }
    }
}