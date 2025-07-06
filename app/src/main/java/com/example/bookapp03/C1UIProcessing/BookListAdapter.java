/**
 * モジュール名: 本一覧アダプター
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍一覧をRecyclerViewで表示するためのアダプタークラス。
 * 履歴:
 *   2025/06/09 横山葉 新規作成
 */

package com.example.bookapp03.C1UIProcessing;

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
import com.example.bookapp03.C3BookInformationProcessing.BookSummaryData;
import com.example.bookapp03.R;

import java.util.List;

/**
 * 書籍データをRecyclerViewに表示するアダプター
 */
public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookViewHolder> {

    /**
     * 書籍サマリのリスト
     */
    private List<BookSummaryData> bookList;

    /**
     * ユーザーID（公開切り替え時に使用）
     */
    private final String uid;

    /**
     * 公開・非公開切り替え操作のハンドラ
     */
    private final PublicPrivateToggleHandler toggleHandler;

    /**
     * コンストラクタ
     * @param bookList 書籍リスト
     * @param uid ユーザーID
     * @param toggleHandler 公開・非公開切り替えハンドラ
     */
    public BookListAdapter(List<BookSummaryData> bookList, String uid, PublicPrivateToggleHandler toggleHandler) {
        this.bookList = bookList;
        this.uid = uid;
        this.toggleHandler = toggleHandler;
    }

    /**
     * 書籍リストを更新
     * @param newBookList 新しいリスト
     */
    public void updateBookList(List<BookSummaryData> newBookList) {
        this.bookList = newBookList;
        notifyDataSetChanged();
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
        holder.titleView.setText(book.getTitle());

        Glide.with(holder.coverView.getContext())
                .load(book.getImageUrl())
                .into(holder.coverView);

        holder.publicSwitch.setChecked(book.isPublic());

        holder.publicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            book.setPublic(isChecked);
            toggleHandler.handleToggle(uid, book.getVolumeId(), isChecked);
        });

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

    /**
     * 各書籍アイテムを保持するViewHolder
     */
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
