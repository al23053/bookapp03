/**
 * モジュール名: 本一覧アダプター
 * 作成者: 横山葉
 * 作成日: 2025/06/09
 * 概要: 書籍一覧をRecyclerViewで表示するためのアダプタークラス。
 * 履歴:
 * 2025/06/09 横山葉 新規作成
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
 * 書籍一覧の表示を担当するRecyclerViewアダプター。
 * BookSummaryDataのリストをRecyclerViewの各アイテムにバインドし、
 * 公開・非公開状態の切り替えや詳細画面への遷移を管理します。
 */
public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookViewHolder> {

    /** 表示する書籍の要約データリスト */
    private List<BookSummaryData> bookList;
    /** 書籍の公開・非公開状態の切り替えをViewModelに伝えるハンドラ */
    private final PublicPrivateToggleHandler toggleHandler;

    /**
     * BookListAdapterのコンストラクタ。
     *
     * @param bookList      表示する書籍の要約データリスト
     * @param toggleHandler 公開・非公開状態の切り替えハンドラ
     */
    public BookListAdapter(List<BookSummaryData> bookList, PublicPrivateToggleHandler toggleHandler) {
        this.bookList = bookList;
        this.toggleHandler = toggleHandler;
    }

    /**
     * データセットを更新するメソッド。
     * ViewModelのLiveDataを監視してこのメソッドを呼ぶことで、RecyclerViewを更新できます。
     *
     * @param newBookList 新しい書籍リスト
     */
    public void updateBookList(List<BookSummaryData> newBookList) {
        this.bookList = newBookList;
        notifyDataSetChanged(); // データが変更されたことをアダプターに通知
    }

    /**
     * 新しいViewHolderインスタンスを作成する。
     * RecyclerViewによって呼び出される。
     *
     * @param parent   新しいViewが追加されるViewGroup
     * @param viewType 新しいViewのタイプ
     * @return 新しく作成されたBookViewHolderインスタンス
     */
    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // item_book.xmlのレイアウトをインフレートしてViewを生成
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    /**
     * 指定された位置のデータをViewHolderにバインドする。
     * RecyclerViewによって呼び出される。
     *
     * @param holder   データを保持するBookViewHolderインスタンス
     * @param position リスト内のアイテムの位置
     */
    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        BookSummaryData book = bookList.get(position);

        // タイトルを設定
        holder.titleView.setText(book.getTitle());

        // カバー画像を設定（Glideライブラリを使用）
        Glide.with(holder.coverView.getContext())
                .load(book.getImageUrl())
                .into(holder.coverView);

        // 公開・非公開スイッチの状態を設定
        holder.publicSwitch.setChecked(book.isPublic());

        // スイッチの変更リスナーを設定
        holder.publicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // BookSummaryDataの公開状態も更新
            book.setPublic(isChecked);
            // Handlerを介してViewModelに公開状態の更新を伝える
            // TODO: ここでユーザーUIDを渡す必要がある (Firebase Authなどから取得) (横山葉)
            toggleHandler.handleToggle("current_user_id", book.getVolumeId(), isChecked);
        });

        // 表紙タップで詳細画面へ遷移するOnClickListenerを設定
        holder.coverView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, BookDetailActivity.class);
            intent.putExtra("volumeId", book.getVolumeId()); // 書籍IDをインテントに格納
            context.startActivity(intent); // 詳細アクティビティを開始
        });
    }

    /**
     * RecyclerViewに表示されるアイテムの総数を返す。
     *
     * @return アイテムの総数
     */
    @Override
    public int getItemCount() {
        return bookList.size();
    }

    /**
     * 各リストアイテムのビューを保持するViewHolderクラス。
     */
    static class BookViewHolder extends RecyclerView.ViewHolder {
        /** 書籍タイトルを表示するTextView */
        TextView titleView;
        /** 書籍カバー画像を表示するImageView */
        ImageView coverView;
        /** 書籍の公開・非公開状態を切り替えるSwitch */
        Switch publicSwitch;

        /**
         * BookViewHolderのコンストラクタ。
         *
         * @param itemView 各リストアイテムのルートビュー
         */
        BookViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.book_title);
            coverView = itemView.findViewById(R.id.book_cover);
            publicSwitch = itemView.findViewById(R.id.publicPrivateSwitch);
        }
    }
}