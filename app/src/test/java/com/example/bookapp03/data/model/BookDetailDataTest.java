package com.example.bookapp03.data.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BookDetailDataTest {

    @Test
    void testBookDetailDataCreationAndGetters() {
        String volumeId = "vol001";
        String name = "Test Book Name";
        String summary = "This is a test summary.";
        String coverImageUrl = "http://example.com/cover.jpg";
        String publicStatus = "public";

        BookDetailData bookDetailData = new BookDetailData(volumeId, name, summary, coverImageUrl, publicStatus);

        assertEquals(volumeId, bookDetailData.getVolumeId());
        assertEquals(name, bookDetailData.getName());
        assertEquals(summary, bookDetailData.getSummary());
        assertEquals(coverImageUrl, bookDetailData.getCoverImageUrl());
        assertEquals(publicStatus, bookDetailData.getPublicStatus());
    }
}