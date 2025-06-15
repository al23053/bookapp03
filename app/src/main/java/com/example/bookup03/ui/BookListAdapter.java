/**
 * モジュール名: BookListAdapter
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍一覧をRecyclerViewで表示するためのアダプタークラス
 * 履歴:
 * 2025/06/09 横山葉 新規作成
 */

package com.example.bookup03.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookup03.data.BookListViewModel;
import com.example.bookup03.data.BookSummaryData;
import com.example.bookup03.R;

import java.util.List;

/**
 * 書籍一覧の表示を担当するRecyclerViewアダプター
 */
public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookViewHolder> {

    private List<BookSummaryData> bookList;
    private BookListViewModel viewModel;

    public BookListAdapter(List<BookSummaryData> bookList, BookListViewModel viewModel) {
        this.bookList = bookList;
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BookViewHolder holder, int position) {
        BookSummaryData book = bookList.get(position);

        // タイトル表示
        holder.titleView.setText(book.getTitle());

        // 表紙画像読み込み
        Glide.with(holder.coverView.getContext())
                .load(book.getImageUrl())
                .into(holder.coverView);

        // 公開・非公開スイッチ状態設定
        holder.publicSwitch.setChecked(book.isPublic());

        // スイッチの変更リスナー
        holder.publicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            book.setPublic(isChecked);
            new PublicPrivateToggleHandler(viewModel).handleToggle(book.getVolumeId(), isChecked);
        });

        // 表紙タップで詳細画面へ
        holder.coverView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, BookDetailActivity.class);
            intent.putExtra("volumeId", book.getVolumeId());
            intent.putExtra("title", book.getTitle());
            intent.putExtra("coverImageUrl", book.getImageUrl());
            intent.putExtra("publicStatus", book.isPublic() ? "public" : "private");
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        ImageView coverView;
        Switch publicSwitch;

        BookViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.book_title);
            coverView = itemView.findViewById(R.id.book_cover);
            publicSwitch = itemView.findViewById(R.id.publicPrivateSwitch);
        }
    }
}