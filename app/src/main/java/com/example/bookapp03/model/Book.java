package com.example.bookapp03.model;

import com.google.gson.annotations.SerializedName; // Gsonを使っている場合に必要

import java.io.Serializable; // Intentでオブジェクトを渡す場合に必要
import java.util.List;
import java.util.Objects;

/**
 * 書籍情報を表現するモデルクラスです。
 * Google Books APIや楽天ブックスAPI、Firestoreから取得した書籍データを格納します。
 * Intentでオブジェクトを渡すためにSerializableを実装しています。
 */
public class Book implements Serializable {

    /**
     * 書籍の一意なID。Google Books APIのidが主に使用されます。
     */
    private String id;

    /**
     * 書籍のタイトル。
     */
    private String title;

    /**
     * 書籍の著者名。複数の著者がいる場合は、代表的な著者が格納されることがあります。
     */
    private String author;

    /**
     * 書籍の出版日。
     */
    private String publishedDate;

    /**
     * 書籍の概要または説明。
     */
    private String description;

    /**
     * 書籍のサムネイル画像のURL。
     */
    private String thumbnailUrl;

    /**
     * 書籍のカテゴリ（ジャンル）のリスト。
     */
    private List<String> categories;

    /**
     * Google Booksの書籍詳細ページへのリンクURL。
     */
    private String infoLink;

    /**
     * 楽天ブックスの商品ページへのURL。GsonでJSONの"itemUrl"フィールドにマッピングされます。
     */
    @SerializedName("itemUrl")
    private String rakutenItemUrl;

    /**
     * 楽天ブックスの大型サムネイル画像のURL。GsonでJSONの"largeImageUrl"フィールドにマッピングされます。
     */
    @SerializedName("largeImageUrl")
    private String rakutenLargeImageUrl;

    /**
     * Firestoreから直接オブジェクトをマッピングするために必要となる、引数なしのデフォルトコンストラクタです。
     */
    public Book() {
        // デフォルトコンストラクタ
    }

    /**
     * 書籍の基本情報を含むコンストラクタです。
     *
     * @param id           書籍の一意なID
     * @param title        書籍のタイトル
     * @param author       書籍の著者名
     * @param description  書籍の概要
     * @param thumbnailUrl 書籍のサムネイル画像のURL
     * @param categories   書籍のカテゴリ（ジャンル）のリスト
     */
    public Book(String id, String title, String author, String description, String thumbnailUrl, List<String> categories) {
        this.id = id;
        this.title = title;
        this.author = author;
        // 注意: このコンストラクタでは publishedDate, infoLink は初期化されていません。
        // 必要であれば引数に追加するか、setterを使用してください。
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.categories = categories;
        // this.publishedDate = publishedDate; // 引数にないためコメントアウトまたは追加
        // this.infoLink = infoLink; // 引数にないためコメントアウトまたは追加
    }

    /**
     * 書籍の一意なIDを取得します。
     *
     * @return 書籍ID
     */
    public String getId() {
        return id;
    }

    /**
     * 書籍のタイトルを取得します。
     *
     * @return 書籍タイトル
     */
    public String getTitle() {
        return title;
    }

    /**
     * 書籍の著者名を取得します。
     *
     * @return 著者名
     */
    public String getAuthor() {
        return author;
    }

    /**
     * 書籍の概要または説明を取得します。
     *
     * @return 書籍の概要
     */
    public String getDescription() {
        return description;
    }

    /**
     * 書籍のサムネイル画像のURLを取得します。
     *
     * @return サムネイル画像のURL
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * 書籍のカテゴリ（ジャンル）のリストを取得します。
     *
     * @return カテゴリのリスト
     */
    public List<String> getCategories() {
        return categories;
    }

    /**
     * 書籍の一意なIDを設定します。
     *
     * @param id 設定する書籍ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 書籍のタイトルを設定します。
     *
     * @param title 設定する書籍タイトル
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 書籍の著者名を設定します。
     *
     * @param author 設定する著者名
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * 書籍の出版日を設定します。
     *
     * @param publishedDate 設定する出版日
     */
    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    /**
     * 書籍の概要または説明を設定します。
     *
     * @param description 設定する書籍の概要
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 書籍のサムネイル画像のURLを設定します。
     *
     * @param thumbnailUrl 設定するサムネイル画像のURL
     */
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     * 書籍のカテゴリ（ジャンル）のリストを設定します。
     *
     * @param categories 設定するカテゴリのリスト
     */
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    /**
     * Google Booksの書籍詳細ページへのリンクURLを設定します。
     *
     * @param infoLink 設定する詳細ページへのリンクURL
     */
    public void setInfoLink(String infoLink) {
        this.infoLink = infoLink;
    }

    /**
     * 楽天ブックスの商品ページへのURLを設定します。
     *
     * @param rakutenItemUrl 設定する楽天商品ページURL
     */
    public void setRakutenItemUrl(String rakutenItemUrl) {
        this.rakutenItemUrl = rakutenItemUrl;
    }

    /**
     * 楽天ブックスの大型サムネイル画像のURLを設定します。
     *
     * @param rakutenLargeImageUrl 設定する楽天大型サムネイル画像URL
     */
    public void setRakutenLargeImageUrl(String rakutenLargeImageUrl) {
        this.rakutenLargeImageUrl = rakutenLargeImageUrl;
    }

    /**
     * オブジェクトの等価性を判断します。書籍ID (id) に基づいて一意性を判断します。
     *
     * @param o 比較対象のオブジェクト
     * @return オブジェクトが等しい場合はtrue、そうでない場合はfalse
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id); // IDで一意性を判断
    }

    /**
     * オブジェクトのハッシュコードを生成します。書籍ID (id) に基づいてハッシュコードを生成します。
     *
     * @return オブジェクトのハッシュコード
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
