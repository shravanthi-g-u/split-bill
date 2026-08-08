package com.shravanthi.split_bill.service;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OcrItemDraft {
    private String name;
    private Double price; // null if we couldn't confidently extract a number
}