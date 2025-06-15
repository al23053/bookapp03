package com.example.bookapp03.C6BookInformationManaging;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * モジュール名: Volume ID プロバイダ
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: Google Books API を使用し、ISBNまたは検索キーワードから最初のボリュームIDを取得するユーティリティクラス
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 */
public class VolumeIdProvider {
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * 指定されたISBNまたは検索キーワードに基づき、
     * Google Books APIから最初にヒットしたvolumeIdを取得する。
     *
     * @param isbn ISBNコードまたは検索キーワード
     * @return 見つかった最初の volumeId。該当なしまたはエラー時は空文字を返す。
     */
    public static String fetchVolumeId(String isbn) {
        try {
            Request req = new Request.Builder()
                    .url(BASE_URL + isbn)
                    .build();
            Response res = client.newCall(req).execute();
            if (!res.isSuccessful()) return "";
            JSONObject root = new JSONObject(res.body().string());
            JSONArray items = root.optJSONArray("items");
            if (items != null && items.length() > 0) {
                return items.getJSONObject(0).optString("id", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
