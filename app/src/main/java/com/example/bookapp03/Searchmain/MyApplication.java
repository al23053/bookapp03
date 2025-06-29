package com.example.bookapp03.Searchmain;

import android.app.Application;

import com.google.firebase.FirebaseApp;

/**
 * アプリケーション全体の状態を管理するためのカスタムApplicationクラスです。
 * アプリケーションの起動時に一度だけ実行される初期化処理（Firebaseの初期化）を行います。
 */
public class MyApplication extends Application {
    /**
     * アプリケーションが作成される際に呼び出されるライフサイクルメソッドです。
     * アプリケーションのグローバルな初期化処理をここで行います。
     */
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
