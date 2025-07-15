package com.example.bookapp03.C6BookInformationManaging;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import android.util.Log;

/**
 * モジュール名: VolumeIDプロバイダ
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
     * 指定されたISBNまたは検索キーワードに基づき、Google Books APIから最初にヒットしたvolumeIdを取得する。
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

    /**
     * 指定されたvolumeIdに基づき、Google Books APIから書籍名を取得する。
     * @param volumeId 書籍のボリュームID
     * @return 書籍名。取得できない場合は空文字。
     */
    public static String fetchBookName(String volumeId) {
        String url = "https://www.googleapis.com/books/v1/volumes/" + volumeId;
        try {
            Request req = new Request.Builder().url(url).build();
            Response res = client.newCall(req).execute();
            if (!res.isSuccessful()) return "";

            JSONObject root = new JSONObject(res.body().string());
            JSONObject volumeInfo = root.optJSONObject("volumeInfo");
            if (volumeInfo != null) {
                return volumeInfo.optString("title", "");
            }
        } catch (Exception e) {
            Log.e("VolumeIdProvider", "Error fetching book name for " + volumeId + ": " + e.getMessage());
        }
        return "";
    }

    /**
     * 指定されたvolumeIdに基づき、Google Books APIから書籍のカバー画像URLを取得する。
     * @param volumeId 書籍のボリュームID
     * @return カバー画像のURL。取得できない場合は空文字。
     */
    public static String fetchCoverImageUrl(String volumeId) {
        String url = "https://www.googleapis.com/books/v1/volumes/" + volumeId;
        try {
            Request req = new Request.Builder().url(url).build();
            Response res = client.newCall(req).execute();
            if (!res.isSuccessful()) return "";

            JSONObject root = new JSONObject(res.body().string());
            JSONObject volumeInfo = root.optJSONObject("volumeInfo");
            if (volumeInfo != null) {
                JSONObject imageLinks = volumeInfo.optJSONObject("imageLinks");
                if (imageLinks != null) {
                    // thumbnail, smallThumbnail, medium, large, extraLargeなどがある
                    return imageLinks.optString("thumbnail", "");
                }
            }
        } catch (Exception e) {
            Log.e("VolumeIdProvider", "Error fetching cover image URL for " + volumeId + ": " + e.getMessage());
        }
        return "";
    }
}
