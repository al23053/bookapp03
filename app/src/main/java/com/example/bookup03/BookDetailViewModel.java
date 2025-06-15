package com.example.bookup03;

public class BookDetailViewModel {

    private BookDetailData detail;

    public BookDetailData getDetail() {
        return detail;
    }

    public void setDetail(BookDetailData detail) {
        this.detail = detail;
    }

    public boolean hasDetail() {
        return detail != null;
    }
}