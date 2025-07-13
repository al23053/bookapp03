/**
 * モジュール名: ControlBackButton
 * 作成者: 三浦寛生
 * 作成日: 2025/06/15
 * 概要: 戻る処理用のボタンの処理を行うクラス。
 * 履歴:
 * 2025/06/15 三浦寛生 新規作成
 */
package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
public class ControlBackButton {
    /**
     * 指定されたボタンに、クリック時に指定されたActivityを終了するリスナーを設定します。
     *
     * @param button クリックリスナーを設定するButtonインスタンス
     * @param activity 終了させたいActivityインスタンス
     */
    public static void setupBackButton(Button button, final Activity activity) {
        if (button != null && activity != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.finish(); // Activityを終了する
                }
            });
        }
    }
}
