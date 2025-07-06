/**
 * モジュール名: ローカルアカウントストア
 * 作成者: 鶴田凌
 * 作成日: 2025/07/10
 * 概要: 端末内 SharedPreferences にユーザ UID リストを保存・取得するクラス
 * 履歴:
 *   2025/07/06 鶴田凌 新規作成
 */
package com.example.bookapp03.C2UserInformationProcessing;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalAccountStore {
    /** SharedPreferences のファイル名 */
    private static final String PREFS_NAME   = "LocalAccountStorePrefs";
    /** UIDリスト保存用キー */
    private static final String KEY_UID_LIST = "key_uid_list";
    /** リストを文字列化する際の区切り文字 */
    private static final String DELIMITER    = ",";

    /**
     * 端末に保存済みの UID リストを取得する
     *
     * @param context Context
     * @return 保存されている UID の List。未保存時は空リストを返す。
     */
    public static List<String> loadUids(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String joined = prefs.getString(KEY_UID_LIST, "");
        if (joined.isEmpty()) {
            return new ArrayList<>();
        }
        String[] array = joined.split(DELIMITER);
        return new ArrayList<>(Arrays.asList(array));
    }

    /**
     * UID をリストに追加して保存する
     *
     * @param context Context
     * @param uid     追加するユーザ UID
     */
    public static void addUid(Context context, String uid) {
        List<String> list = loadUids(context);
        if (!list.contains(uid)) {
            list.add(uid);
            saveList(context, list);
        }
    }

    /**
     * UID をリストから削除して保存する
     *
     * @param context Context
     * @param uid     削除するユーザ UID
     */
    public static void removeUid(Context context, String uid) {
        List<String> list = loadUids(context);
        if (list.remove(uid)) {
            saveList(context, list);
        }
    }

    /**
     * UID リストを SharedPreferences に書き込む
     *
     * @param context Context
     * @param list    保存対象の UID リスト
     */
    private static void saveList(Context context, List<String> list) {
        String joined = String.join(DELIMITER, list);
        SharedPreferences.Editor ed = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        ed.putString(KEY_UID_LIST, joined);
        ed.apply();
    }
}
