package com.example.bookup03;

public class BookDetailData {
    private String volumeId;
    private String name;
    private String summary;
    private String coverImageUrl;
    private String publicStatus; // "public" or "private"

    public BookDetailData(String volumeId, String name, String summary, String coverImageUrl, String publicStatus) {
        this.volumeId = volumeId;
        this.name = name;
        this.summary = summary;
        this.coverImageUrl = coverImageUrl;
        this.publicStatus = publicStatus;
    }

    public String getVolumeId() { return volumeId; }
    public String getName() { return name; }
    public String getSummary() { return summary; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public String getPublicStatus() { return publicStatus; }
}