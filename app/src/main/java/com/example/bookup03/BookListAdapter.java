package com.example.bookup03;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookViewHolder> {

    private List<BookSummaryData> bookList;
    private BookListViewModel viewModel;

    // ViewModelを受け取れるようにする
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

        holder.titleView.setText(book.getTitle());

        Glide.with(holder.coverView.getContext())
                .load(book.getImageUrl())
                .into(holder.coverView);

        // スイッチ状態設定
        holder.publicSwitch.setChecked(book.isPublic());

        // リスナー設定（状態変更時に ViewModel に通知）
        holder.publicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            book.setPublic(isChecked); // モデルの状態も更新

            // 公開・非公開切替処理をViewModelに渡す
            new PublicPrivateToggleHandler(viewModel).handleToggle(book.getVolumeId(), isChecked);
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