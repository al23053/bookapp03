/**
 * モジュール名: チュートリアル管理
 * 作成者: 横山葉
 * 作成日: 2025/06/20
 * 概要: アプリケーションのチュートリアル表示状態を管理し、必要に応じてチュートリアルを表示するクラス。
 * 履歴:
 * 2025/06/20 横山葉 新規作成
 */
package com.example.bookapp03.C1UIProcessing;

import android.content.Context;
import android.content.SharedPreferences;
import android.app.Activity;
import android.app.AlertDialog;

/**
 * アプリケーションのチュートリアル表示のロジックを管理するクラス。
 * チュートリアルをすでに表示したかどうかをSharedPreferencesに保存し、
 * 必要に応じてチュートリアル（今回はAlertDialogの例）を表示します。
 */
public class TutorialManager {
    /** アプリケーションコンテキスト */
    private final Context context;
    /** チュートリアル表示状態を保存するためのSharedPreferencesインスタンス */
    private final SharedPreferences prefs;
    /** チュートリアル表示状態を保存するSharedPreferencesのキー */
    private static final String KEY_SHOWN = "tutorial_shown";

    /**
     * TutorialManagerのコンストラクタ。
     * コンテキストを受け取り、SharedPreferencesを初期化します。
     *
     * @param context アプリケーションまたはアクティビティのコンテキスト
     */
    public TutorialManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("tutorial_pref", Context.MODE_PRIVATE);
    }

    /**
     * チュートリアルを表示するべきかどうかを判定する。
     * チュートリアルがまだ表示されていない場合にtrueを返す。
     *
     * @return チュートリアルを表示するべきであればtrue、そうでなければfalse
     */
    public boolean shouldShowTutorial() {
        return !prefs.getBoolean(KEY_SHOWN, false);
    }

    /**
     * チュートリアルを表示済みとしてマークする。
     * このメソッドが呼び出された後、shouldShowTutorial()はfalseを返すようになる。
     */
    public void markTutorialAsShown() {
        prefs.edit().putBoolean(KEY_SHOWN, true).apply();
    }

    /**
     * チュートリアルがまだ表示されていない場合、チュートリアルを表示する。
     * チュートリアル表示後、または表示不要な場合にonComplete Runnableを実行する。
     *
     * @param activity   チュートリアルダイアログを表示するアクティビティ
     * @param onComplete チュートリアル完了後またはスキップ後に実行されるRunnable
     */
    public void showTutorialIfNeeded(Activity activity, Runnable onComplete) {
        // チュートリアルを表示する必要があるか確認
        if (shouldShowTutorial()) {
            // チュートリアルダイアログを表示する例
            new AlertDialog.Builder(activity)
                    .setTitle("使い方") // ダイアログのタイトルを設定
                    .setMessage("ここで使い方を説明する") // ダイアログのメッセージを設定
                    .setPositiveButton("OK", (dialog, which) -> {
                        // 「OK」ボタンがクリックされた場合、チュートリアルを表示済みにマークし、完了Runnableを実行
                        markTutorialAsShown();
                        onComplete.run();
                    })
                    .setNegativeButton("スキップ", (dialog, which) -> {
                        // 「スキップ」ボタンがクリックされた場合、完了Runnableを実行（チュートリアルは表示済みとしない）
                        onComplete.run();
                    })
                    .show(); // ダイアログを表示
        } else {
            // チュートリアルを表示する必要がない場合、直ちに完了Runnableを実行
            onComplete.run();
        }
    }
}