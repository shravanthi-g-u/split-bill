package com.shravanthi.split_bill.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OcrServiceTest {

    private final OcrService ocrService = new OcrService();

    // Real raw text captured from OCR.space (Engine 2) on an actual receipt,
    // used here so the parser is tested against real-world formatting quirks.
    private static final String SAMPLE_RAW_TEXT =
            "SI.      Product/Particulars     HSN Code        Oty     Price   Dis%   Dis Amt  Net Rate        Amount  \r\n" +
            "1 SHORTS 6109    1.00    3499.00 50.00   1749.50 1749.50 1749.50 =      \r\n" +
            "2 TRACK PANT     6109    1.00    3999.00 50.00   1999.50 1999.50 1999.50\r\n" +
            "3 SHIRT  6205    1.00    4999.00 40.00   1999.60 2999.40 2999.40.       \r\n" +
            "4 T-SHIRT        6109    1.00    6320.00 50.00   3160.00 3160.00 3160.00\r\n" +
            "Sub Total        12.00   11906.60        \r\n" +
            "48502.40 \r\n";

    @Test
    void extractsCorrectNumberOfItems() {
        List<OcrItemDraft> drafts = ocrService.extractItemDrafts(SAMPLE_RAW_TEXT);
        assertEquals(4, drafts.size());
    }

    @Test
    void extractsCorrectItemNames() {
        List<OcrItemDraft> drafts = ocrService.extractItemDrafts(SAMPLE_RAW_TEXT);

        assertEquals("SHORTS", drafts.get(0).getName());
        assertEquals("TRACK PANT", drafts.get(1).getName());
        assertEquals("SHIRT", drafts.get(2).getName());
        assertEquals("T-SHIRT", drafts.get(3).getName());
    }

    @Test
    void extractsCorrectPrices() {
        List<OcrItemDraft> drafts = ocrService.extractItemDrafts(SAMPLE_RAW_TEXT);

        assertEquals(1749.50, drafts.get(0).getPrice(), 0.001);
        assertEquals(1999.50, drafts.get(1).getPrice(), 0.001);
        assertEquals(2999.40, drafts.get(2).getPrice(), 0.001);
        assertEquals(3160.00, drafts.get(3).getPrice(), 0.001);
    }

    @Test
    void ignoresHeaderAndFooterLines() {
        List<OcrItemDraft> drafts = ocrService.extractItemDrafts(SAMPLE_RAW_TEXT);

        // "Sub Total" and the trailing "48502.40" line must NOT be treated as items
        boolean hasSubTotal = drafts.stream().anyMatch(d -> d.getName().toLowerCase().contains("sub total"));
        boolean hasHeaderRow = drafts.stream().anyMatch(d -> d.getName().toLowerCase().contains("particulars"));

        assertFalse(hasSubTotal);
        assertFalse(hasHeaderRow);
    }

    @Test
    void returnsEmptyListForBlankText() {
        List<OcrItemDraft> drafts = ocrService.extractItemDrafts("");
        assertTrue(drafts.isEmpty());
    }

    @Test
    void handlesTextWithNoValidItemRows() {
        String noiseOnly = "Random Header Text\r\nAnother line with no numbers\r\n";
        List<OcrItemDraft> drafts = ocrService.extractItemDrafts(noiseOnly);
        assertTrue(drafts.isEmpty());
    }
}