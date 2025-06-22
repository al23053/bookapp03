package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.bookapp03.C2UserInformationProcessing.UserInfo;
import com.example.bookapp03.C2UserInformationProcessing.TransmitAccountSwitching;

/**
 * モジュール名: アカウント切替制御
 * 作成者: 鶴田凌
 * 作成日: 2025/06/16
 * 概要: RadioGroup の選択変更を検知し、TransmitAccountSwitching 経由で切替処理を呼び出す
 * 履歴:
 * 2025/06/16 鶴田凌 新規作成
 */
public class ControlAccountSwitching {
    private final Activity activity;
    private final RadioGroup group;

    /**
     * @param activity このコントローラを使う Activity
     * @param group    選択肢を表示した RadioGroup
     */
    public ControlAccountSwitching(Activity activity, RadioGroup group) {
        this.activity = activity;
        this.group = group;
    }

    /**
     * RadioGroup のチェック変更リスナーを設定する。
     * 選択された UserInfo を TransmitAccountSwitching に渡し、
     * 切替成功/失敗を Toast で表示する。
     */
    public void bind() {
        group.setOnCheckedChangeListener((rg, checkedId) -> {
            RadioGroup.LayoutParams lp =
                    (RadioGroup.LayoutParams) rg.findViewById(checkedId).getLayoutParams();
            Object tag = rg.findViewById(checkedId).getTag();
            if (!(tag instanceof UserInfo)) return;
            UserInfo info = (UserInfo) tag;
            boolean ok = new TransmitAccountSwitching(activity)
                    .transmitAccountSwitching(info);
            Toast.makeText(activity,
                    ok ? "切替成功" : "切替失敗",
                    Toast.LENGTH_SHORT
            ).show();
            if (ok) activity.finish();
        });
    }
}
