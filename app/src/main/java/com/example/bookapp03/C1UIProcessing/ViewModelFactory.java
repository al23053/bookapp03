/**
 * モジュール名: ViewModelFactory
 * 作成者: 横山葉
 * 作成日: 2025/06/20
 * 概要: ViewModelのインスタンスを生成するためのファクトリークラス。依存性を注入する。
 * 履歴:
 * 2025/06/20 横山葉 新規作成
 */
package com.example.bookapp03.C1UIProcessing;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookapp03.C6BookInformationManaging.BookRepositoryImpl;
import com.example.bookapp03.C6BookInformationManaging.BookRepository;
import com.example.bookapp03.C3BookInformationProcessing.BookDetailViewModel;
import com.example.bookapp03.C3BookInformationProcessing.BookListViewModel;

/**
 * ViewModelのインスタンスを生成するためのファクトリークラス。
 * ViewModelが依存するRepositoryなどをここで注入する。
 */
public class ViewModelFactory implements ViewModelProvider.Factory {

    private final Context applicationContext; // アプリケーションコンテキストを保持

    public ViewModelFactory(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // BookRepositoryの実装を生成 (シングルトンパターンなどで管理することも推奨)
        BookRepository bookRepository = new BookRepositoryImpl(applicationContext);

        if (modelClass.isAssignableFrom(BookListViewModel.class)) {
            return (T) new BookListViewModel(bookRepository);
        } else if (modelClass.isAssignableFrom(BookDetailViewModel.class)) {
            return (T) new BookDetailViewModel(bookRepository);
        }
        // 他のViewModelが必要になったらここに追加
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}