package com.example.bookup03;

public class BookSummaryData {
    private String volumeId;
    private String title;
    private String imageUrl;
    private boolean isPublic;

    public BookSummaryData(String volumeId, String title, String imageUrl) {
        this.volumeId = volumeId;
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getVolumeId() { return volumeId; }
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}