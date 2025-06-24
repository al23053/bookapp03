package com.example.bookapp03.data.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HighlightMemoDataTest {

    @Test
    void testHighlightMemoDataCreationAndGetters() {
        int page = 10;
        int line = 5;
        String memoContent = "Important highlight memo.";

        HighlightMemoData memoData = new HighlightMemoData(page, line, memoContent);

        assertEquals(page, memoData.getPage());
        assertEquals(line, memoData.getLine());
        assertEquals(memoContent, memoData.getMemoContent());
    }
}