package com.example.bookapp03.C5UserInformationManaging;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * モジュール名: ユーザ認証管理
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: Firebase Authentication を使用して現在ログイン中のユーザ UID を取得するユーティリティクラス
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
public class UserAuthManager {
    /**
     * FirebaseAuth のシングルトンインスタンス
     */
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    /**
     * 現在ログイン中のユーザ UID を返す。
     *
     * @return UID 文字列。ログインしていなければ空文字を返す。
     */
    public static String getCurrentUid() {
        FirebaseUser user = auth.getCurrentUser();
        return (user != null) ? user.getUid() : "";
    }
}
