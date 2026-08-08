package com.shravanthi.split_bill.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BillSummaryResponse {
    private Map<String, Map<String, Double>> perBill;
    private Map<String, Double> combinedTotals;
}
