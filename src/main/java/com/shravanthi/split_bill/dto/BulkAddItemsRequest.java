package com.shravanthi.split_bill.dto;

import lombok.Data;

import java.util.List;

@Data
public class BulkAddItemsRequest {
    private List<AddItemRequest> items;
}