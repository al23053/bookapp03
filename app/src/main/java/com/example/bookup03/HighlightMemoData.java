package com.example.bookup03;

public class HighlightMemoData {
    private String page;
    private String line;
    private String memoContent;

    public HighlightMemoData(String page, String line, String memoContent) {
        this.page = page;
        this.line = line;
        this.memoContent = memoContent;
    }

    public String getPage() { return page; }
    public String getLine() { return line; }
    public String getMemoContent() { return memoContent; }
}