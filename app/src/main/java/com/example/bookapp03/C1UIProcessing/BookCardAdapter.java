/**
 * モジュール名: BookCardAdapter
 * 作成者: 三浦寛生
 * 作成日: 2025/06/30
 * 概要:　書籍情報をカード形式で表示するためのRecyclerView用アダプターです。
 * 横スクロールリストなどで書籍のサムネイルとタイトルを表示します。
 * 履歴:
 * 2025/06/30 三浦寛生 新規作成
 */
package com.example.bookapp03.C1UIProcessing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookapp03.R;
import com.example.bookapp03.C4SearchProcessing.Book;

import java.util.List;

public class BookCardAdapter extends RecyclerView.Adapter<BookCardAdapter.BookViewHolder> {

    /**
     * 表示する書籍のリスト。
     */
    private List<Book> bookList;

    /**
     * 書籍カードがクリックされたときに通知されるリスナー。
     */
    private OnBookClickListener listener;

    /**
     * 書籍カードのクリックイベントを処理するためのインターフェースです。
     */
    public interface OnBookClickListener {
        /**
         * 書籍カードがクリックされたときに呼び出されます。
         *
         * @param book クリックされた書籍オブジェクト
         */
        void onBookClick(Book book);
    }

    /**
     * BookCardAdapterのコンストラクタです。
     *
     * @param bookList 表示する書籍のリスト
     * @param listener 書籍カードのクリックリスナー
     */
    public BookCardAdapter(List<Book> bookList, OnBookClickListener listener) {
        this.bookList = bookList;
        this.listener = listener;
    }

    /**
     * アダプターに新しい書籍リストを設定し、表示を更新します。
     *
     * @param newBookList 新しい書籍のリスト
     */
    public void setBookList(List<Book> newBookList) {
        this.bookList = newBookList;
        notifyDataSetChanged();
    }

    /**
     * ViewHolderを作成します。レイアウトをインフレートし、ViewHolderを初期化します。
     *
     * @param parent   ViewHolderが属するViewGroup
     * @param viewType ビューのタイプ（使用しない）
     * @return 新しく作成されたBookViewHolder
     */
    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_card, parent, false);
        return new BookViewHolder(view);
    }

    /**
     * ViewHolderにデータをバインドします。特定の位置の書籍データをViewHolderのUI要素に設定します。
     *
     * @param holder   データをバインドするBookViewHolder
     * @param position データを取得するリスト内の位置
     */
    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.bind(book, listener);
    }

    /**
     * アダプターが保持するアイテムの総数を返します。
     *
     * @return アイテムの総数
     */
    @Override
    public int getItemCount() {
        return bookList != null ? bookList.size() : 0;
    }

    /**
     * RecyclerViewの各アイテム（書籍カード）のビューを保持するViewHolderクラスです。
     */
    static class BookViewHolder extends RecyclerView.ViewHolder {
        /**
         * 書籍のサムネイル画像を表示するImageView。
         */
        ImageView bookThumbnailImageView;

        /**
         * 書籍のタイトルを表示するTextView。
         */
        TextView bookTitleTextView;

        /**
         * BookViewHolderのコンストラクタです。
         *
         * @param itemView アイテムのルートビュー
         */
        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookThumbnailImageView = itemView.findViewById(R.id.book_thumbnail_image_view);
            bookTitleTextView = itemView.findViewById(R.id.book_title_text_view);
        }

        /**
         * 指定された書籍データをViewHolderのUI要素にバインドします。
         *
         * @param book     バインドする書籍オブジェクト
         * @param listener アイテムクリックリスナー
         */
        public void bind(final Book book, final OnBookClickListener listener) {
            bookTitleTextView.setText(book.getTitle());

            if (book.getThumbnailUrl() != null && !book.getThumbnailUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(book.getThumbnailUrl())
                        .placeholder(R.drawable.ic_book_placeholder)
                        .error(R.drawable.ic_broken_image)
                        .into(bookThumbnailImageView);
            } else {
                bookThumbnailImageView.setImageResource(R.drawable.ic_book_placeholder);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookClick(book);
                }
            });
        }
    }
}
