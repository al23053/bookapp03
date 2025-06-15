/**
 * モジュール名: UID
 * 作成者: 増田学斗
 * 作成日: 2025/06/15
 * 概要: データベースに保存されるデータのクラス
 * 履歴:
 *   2025/06/15 増田学斗 新規作成
 */

package com.example.a1bapp;
public class UID {
    /** ユーザーのメールアドレス（今回は使わないなら削除してもOK） */
    public String email;

    /** ユーザーの表示名（ニックネームとして使う場合） */
    public String displayName;

    /** Firebase用の空コンストラクタ（必須） */
    public UID() {
    }

    /**
     * コンストラクタ
     * @param email メールアドレス
     * @param displayName 表示名
     */
    public UID(String displayName, String email) {
        this.displayName = displayName;
        this.email = email;
    }
}
