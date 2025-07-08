/**
 * モジュール名: チュートリアル管理
 * 作成者: 横山葉
 * 作成日: 2025/06/20
 * 概要: アプリケーションのチュートリアル表示状態を管理し、必要に応じて画像チュートリアルを表示するクラス。
 * 履歴:
 * 2025/06/20 横山葉 新規作成
 */
package com.example.bookapp03.C1UIProcessing;

import android.content.Context;
import android.content.SharedPreferences;
import android.app.Activity;
import android.content.Intent;

/**
 * アプリケーションのチュートリアル表示のロジックを管理するクラス。
 * チュートリアルがすでに表示されたかをSharedPreferencesで記録し、
 * 初回起動時などに画像形式のチュートリアル画面を起動する。
 */
public class TutorialManager {
    /** コンテキスト */
    private final Context context;
    /** チュートリアル表示状態の保存先 */
    private final SharedPreferences prefs;
    /** チュートリアル表示済みの判定キー */
    private static final String KEY_SHOWN = "tutorial_shown";

    /**
     * TutorialManagerのコンストラクタ。
     *
     * @param context アプリケーションまたはアクティビティのコンテキスト
     */
    public TutorialManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("tutorial_pref", Context.MODE_PRIVATE);
    }

    /**
     * チュートリアルを表示すべきか判定。
     *
     * @return 未表示ならtrue、表示済みならfalse
     */
    public boolean shouldShowTutorial() {
        return !prefs.getBoolean(KEY_SHOWN, false);
    }

    /**
     * チュートリアルを表示済みとして記録。
     */
    public void markTutorialAsShown() {
        prefs.edit().putBoolean(KEY_SHOWN, true).apply();
    }

    /**
     * チュートリアルが未表示の場合、チュートリアルActivityを起動する。
     * チュートリアルが表示された場合でも、onCompleteは即座に呼び出される。
     *
     * @param activity   チュートリアルを起動する元のアクティビティ
     * @param onComplete チュートリアル表示後に実行する処理（省略可）
     */
    public void showTutorialIfNeeded(Activity activity, Runnable onComplete) {
        if (shouldShowTutorial()) {
            Intent intent = new Intent(activity, TutorialActivity.class);
            activity.startActivity(intent);
            markTutorialAsShown();
        }
        if (onComplete != null) {
            onComplete.run();
        }
    }
}
