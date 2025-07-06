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
