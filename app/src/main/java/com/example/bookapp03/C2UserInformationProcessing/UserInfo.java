package com.example.bookapp03.C2UserInformationProcessing;

/**
 * モジュール名: ユーザ情報データクラス
 * 作成者: 鶴田凌
 * 作成日: 2025/06/16
 * 概要: アカウント切替時に渡すユーザ情報を保持するクラス
 * 履歴:
 * 2025/06/16 鶴田凌 新規作成
 */
public class UserInfo {
    /**
     * Firebase UID
     */
    public final String uid;
    /**
     * ニックネーム
     */
    public final String nickname;
    /**
     * Gmail アドレス
     */
    public final String email;

    /**
     * @param uid      Firebase Authentication の UID
     * @param nickname 表示用ニックネーム
     * @param email    Google アカウントのメールアドレス
     */
    public UserInfo(String uid, String nickname, String email) {
        this.uid = uid;
        this.nickname = nickname;
        this.email = email;
    }
}
