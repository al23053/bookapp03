/**
 * モジュール名: BookListAdapter
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍一覧をRecyclerViewで表示するためのアダプタークラス
 * 履歴:
 *   2025/06/09 横山葉 新規作成
 */

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

/**
 * 書籍一覧の表示を担当するRecyclerViewアダプター
 */
public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookViewHolder> {

    /**
     * 表示する書籍リスト
     */
    private List<BookSummaryData> bookList;

    /**
     * 書籍一覧のViewModel
     */
    private BookListViewModel viewModel;

    /**
     * コンストラクタ
     *
     * @param bookList 書籍リスト
     * @param viewModel ビューモデル
     */
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

        // スイッチの変更リスナー設定
        holder.publicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            book.setPublic(isChecked); // モデルも更新

            // ViewModelへ切り替えを通知
            new PublicPrivateToggleHandler(viewModel).handleToggle(book.getVolumeId(), isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    /**
     * 書籍表示用ViewHolder
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
