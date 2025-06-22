package com.example.bookapp03.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.app.Activity;
import android.app.AlertDialog;

public class TutorialManager {
    private final Context context;
    private final SharedPreferences prefs;
    private static final String KEY_SHOWN = "tutorial_shown";

    public TutorialManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("tutorial_pref", Context.MODE_PRIVATE);
    }

    // チュートリアルを表示するべきか
    public boolean shouldShowTutorial() {
        return !prefs.getBoolean(KEY_SHOWN, false);
    }

    // チュートリアルを表示済みにする
    public void markTutorialAsShown() {
        prefs.edit().putBoolean(KEY_SHOWN, true).apply();
    }

    // 実際に表示処理を行う（ActivityやFragment内で呼ぶ）
    public void showTutorialIfNeeded(Activity activity, Runnable onComplete) {
        if (shouldShowTutorial()) {
            // チュートリアルダイアログとか表示
            new AlertDialog.Builder(activity)
                    .setTitle("使い方")
                    .setMessage("ここで使い方を説明する")
                    .setPositiveButton("OK", (dialog, which) -> {
                        markTutorialAsShown();
                        onComplete.run();
                    })
                    .setNegativeButton("スキップ", (dialog, which) -> onComplete.run())
                    .show();
        } else {
            onComplete.run();
        }
    }
}