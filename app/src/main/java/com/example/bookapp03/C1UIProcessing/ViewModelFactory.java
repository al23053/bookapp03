/**
 * モジュール名: 画面表示用モデル生成処理
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
 * アプリケーションコンテキストを必要とするRepositoryの生成に使用されます。
 */
public class ViewModelFactory implements ViewModelProvider.Factory {

    /** アプリケーション全体のコンテキスト */
    private final Context applicationContext;

    /**
     * ViewModelFactoryのコンストラクタ。
     *
     * @param applicationContext ViewModelが依存するRepositoryの初期化に用いられるアプリケーションコンテキスト
     */
    public ViewModelFactory(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 指定されたClassに対応するViewModelの新しいインスタンスを生成する。
     * 必要に応じて依存性（Repositoryなど）を注入する。
     *
     * @param modelClass 生成するViewModelのClassオブジェクト
     * @param <T>        ViewModelの型
     * @return 指定されたClassの新しいViewModelインスタンス
     * @throws IllegalArgumentException 未知のViewModelクラスが要求された場合
     */
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // BookRepositoryの実装を生成
        // ここでシングルトンパターンやDagger/HiltのようなDIフレームワークを使用して管理することも推奨される
        BookRepository bookRepository = new BookRepositoryImpl(applicationContext);

        // 要求されたViewModelのクラスに基づいて適切なインスタンスを生成し、依存性を注入
        if (modelClass.isAssignableFrom(BookListViewModel.class)) {
            // BookListViewModelのインスタンスを生成し、BookRepositoryを渡す
            return (T) new BookListViewModel(bookRepository);
        } else if (modelClass.isAssignableFrom(BookDetailViewModel.class)) {
            // BookDetailViewModelのインスタンスを生成し、BookRepositoryを渡す
            return (T) new BookDetailViewModel(bookRepository);
        }
        // 他のViewModelが必要になった場合は、ここにelse ifブロックを追加する
        // サポートされていないViewModelクラスが要求された場合は例外をスロー
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}