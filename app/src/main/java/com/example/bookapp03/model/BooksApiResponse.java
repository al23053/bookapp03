package com.example.bookapp03.model;

import com.google.gson.annotations.SerializedName; // GsonでJSONフィールドをマッピングするために必要

import java.util.List;

/**
 * Google Books APIからのレスポンス構造を表現するモデルクラスです。
 * 書籍検索結果のトップレベルのコンテナとなります。
 */
public class BooksApiResponse {
    /**
     * 個々の書籍情報アイテムのリストです。
     */
    @SerializedName("items")
    private List<Item> items; // 各書籍の情報

    /**
     * 個々の書籍情報アイテムのリストを取得します。
     *
     * @return 書籍情報アイテムのリスト
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * Google Books APIレスポンス内の個々の書籍情報アイテムを表現するネストされたクラスです。
     */
    public static class Item {
        /**
         * 書籍の一意なID。
         */
        @SerializedName("id")
        private String id;


        /**
         * 書籍の詳細情報を含むオブジェクト。
         */
        @SerializedName("volumeInfo")
        private VolumeInfo volumeInfo; // 書籍の詳細情報


        /**
         * 書籍の一意なIDを取得します。
         *
         * @return 書籍ID
         */
        public String getId() {
            return id;
        }


        /**
         * 書籍の詳細情報を取得します。
         *
         * @return 書籍の詳細情報 (VolumeInfoオブジェクト)
         */
        public VolumeInfo getVolumeInfo() {
            return volumeInfo;
        }
    }

    /**
     * 書籍の詳細情報を表現するネストされたクラスです。
     * タイトル、著者、概要、画像リンクなどが含まれます。
     */
    public static class VolumeInfo {
        /**
         * 書籍のタイトル。
         */
        @SerializedName("title")
        private String title;

        /**
         * 書籍の著者名のリスト。
         */
        @SerializedName("authors")
        private List<String> authors;

        /**
         * 書籍の出版日。
         */
        @SerializedName("publishedDate")
        private String publishedDate;

        /**
         * 書籍の概要または説明。
         */
        @SerializedName("description")
        private String description;

        /**
         * 書籍の画像リンクを含むオブジェクト。
         */
        @SerializedName("imageLinks")
        private ImageLinks imageLinks;

        /**
         * 書籍のカテゴリ（ジャンル）のリスト。
         */
        @SerializedName("categories")
        private List<String> categories;
        // 他にも多くのフィールドがありますが、ここでは最低限に絞っています
        /**
         * 書籍の業界識別子（ISBNなど）のリスト。
         */
        @SerializedName("industryIdentifiers")
        private List<IndustryIdentifier> industryIdentifiers;

        /**
         * 書籍のGoogle Books詳細ページへのリンクURL。
         */
        @SerializedName("infoLink")
        private String infoLink;
        /**
         * 書籍のタイトルを取得します。
         *
         * @return 書籍タイトル
         */
        public String getTitle() {
            return title;
        }

        /**
         * 書籍の著者名のリストを取得します。
         *
         * @return 著者名のリスト
         */
        public List<String> getAuthors() {
            return authors;
        }

        /**
         * 書籍の出版日を取得します。
         *
         * @return 出版日
         */
        public String getPublishedDate() {
            return publishedDate;
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
         * 書籍の画像リンクを取得します。
         *
         * @return 画像リンク (ImageLinksオブジェクト)
         */
        public ImageLinks getImageLinks() {
            return imageLinks;
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
         * 書籍の業界識別子（ISBNなど）のリストを取得します。
         *
         * @return 業界識別子のリスト
         */
        public List<IndustryIdentifier> getIndustryIdentifiers() {
            return industryIdentifiers;
        }

        /**
         * 書籍のGoogle Books詳細ページへのリンクURLを取得します。
         *
         * @return 詳細ページへのリンクURL
         */
        public String getInfoLink() {
            return infoLink;
        }
    }
    /**
     * 書籍の業界識別子（ISBNなど）を表現するネストされたクラスです。
     */
    public static class IndustryIdentifier {
        /**
         * 識別子のタイプ（例: "ISBN_10", "ISBN_13"）。
         */
        @SerializedName("type")
        private String type;

        /**
         * 識別子の値（例: ISBNの番号）。
         */
        @SerializedName("identifier")
        private String identifier;

        /**
         * 識別子のタイプを取得します。
         *
         * @return 識別子のタイプ
         */
        public String getType() {
            return type;
        }

        /**
         * 識別子の値を取得します。
         *
         * @return 識別子の値
         */
        public String getIdentifier() {
            return identifier;
        }
    }
    /**
     * 書籍のサムネイル画像リンクを表現するネストされたクラスです。
     */
    public static class ImageLinks {

        /**
         * 標準サイズのサムネイル画像のURL。
         */
        @SerializedName("thumbnail")
        private String thumbnail;


        /**
         * 標準サイズのサムネイル画像のURLを取得します。
         *
         * @return 標準サイズのサムネイル画像のURL
         */
        public String getThumbnail() {
            return thumbnail;
        }
    }
}
