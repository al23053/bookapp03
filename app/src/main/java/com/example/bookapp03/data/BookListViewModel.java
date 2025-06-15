/**
 * モジュール名: BookListViewModel
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍一覧のデータを管理するViewModelクラス
 * 履歴:
 * 2025/06/09 横山葉 新規作成
 */

package com.example.bookapp03.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 書籍一覧のデータ管理を行うViewModel
 */
public class BookListViewModel {

    /**
     * 書籍サマリのリスト
     */
    private List<BookSummaryData> bookList = new ArrayList<>();

    /**
     * 書籍一覧を取得する
     * @return 書籍サマリのリスト
     */
    public List<BookSummaryData> getBooks() {
        return bookList;
    }

    /**
     * 書籍一覧を設定する
     * @param books 書籍サマリのリスト
     */
    public void setBooks(List<BookSummaryData> books) {
        this.bookList = books;
    }

    /**
     * 書籍一覧が空かどうかを判定する
     * @return true: 空, false: 空でない
     */
    public boolean isEmpty() {
        return bookList == null || bookList.isEmpty();
    }

    /**
     * 指定された書籍の公開状態を更新する
     * @param bookId 書籍ID
     * @param isPublic 公開かどうか
     */
    public void updatePublicStatus(String bookId, boolean isPublic) {
        for (BookSummaryData book : bookList) {
            if (book.getVolumeId().equals(bookId)) {
                book.setPublic(isPublic);
                // ここにDB更新や通信処理などを追加する想定
                System.out.println("公開状態更新: " + book.getTitle() + " → " + (isPublic ? "公開" : "非公開"));
                break;
            }
        }
    }
}
