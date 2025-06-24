package com.example.bookapp03.data.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BookSummaryDataTest {

    @Test
    void testBookSummaryDataCreationAndGettersSetters() {
        String volumeId = "summary001";
        String title = "Summary Title";
        String imageUrl = "http://example.com/summary_cover.png";

        BookSummaryData bookSummaryData = new BookSummaryData(volumeId, title, imageUrl);

        assertEquals(volumeId, bookSummaryData.getVolumeId());
        assertEquals(title, bookSummaryData.getTitle());
        assertEquals(imageUrl, bookSummaryData.getImageUrl());
        assertFalse(bookSummaryData.isPublic()); // デフォルトはfalse

        bookSummaryData.setPublic(true);
        assertTrue(bookSummaryData.isPublic());

        bookSummaryData.setPublic(false);
        assertFalse(bookSummaryData.isPublic());
    }
}