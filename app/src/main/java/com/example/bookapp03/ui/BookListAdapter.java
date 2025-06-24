/**
 * モジュール名: BookListAdapter
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍一覧をRecyclerViewで表示するためのアダプタークラス
 * 履歴:
 * 2025/06/09 横山葉 新規作成
 */

package com.example.bookapp03.ui;

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
import com.example.bookapp03.data.model.BookSummaryData;
import com.example.bookapp03.R;

import java.util.List;

/**
 * 書籍一覧の表示を担当するRecyclerViewアダプター
 */
public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookViewHolder> {

    private List<BookSummaryData> bookList;
    private final PublicPrivateToggleHandler toggleHandler; // ViewModelではなくHandlerを受け取る

    public BookListAdapter(List<BookSummaryData> bookList, PublicPrivateToggleHandler toggleHandler) {
        this.bookList = bookList;
        this.toggleHandler = toggleHandler;
    }

    /**
     * データセットを更新するメソッド
     * ViewModelのLiveDataを監視してこのメソッドを呼ぶことで、RecyclerViewを更新できます。
     * @param newBookList 新しい書籍リスト
     */
    public void updateBookList(List<BookSummaryData> newBookList) {
        this.bookList = newBookList;
        notifyDataSetChanged(); // データが変更されたことをアダプターに通知
    }


    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        BookSummaryData book = bookList.get(position);

        // タイトルを設定
        holder.titleView.setText(book.getTitle());

        // カバー画像を設定（Glideを使用）
        Glide.with(holder.coverView.getContext())
                .load(book.getImageUrl())
                .into(holder.coverView);

        // 公開・非公開スイッチ状態設定
        holder.publicSwitch.setChecked(book.isPublic());

        // スイッチの変更リスナー
        holder.publicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // BookSummaryDataの公開状態も更新
            book.setPublic(isChecked);
            // Handlerを介してViewModelに公開状態の更新を伝える
            // TODO: ここでユーザーUIDを渡す必要がある (Firebase Authなどから取得)
            toggleHandler.handleToggle("current_user_id", book.getVolumeId(), isChecked);
        });

        // 表紙タップで詳細画面へ
        holder.coverView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, BookDetailActivity.class);
            intent.putExtra("volumeId", book.getVolumeId());
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