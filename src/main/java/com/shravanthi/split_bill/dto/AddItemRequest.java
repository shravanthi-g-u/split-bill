package com.shravanthi.split_bill.dto;

import java.util.List;

import lombok.Data;

@Data
public class AddItemRequest {

    private String name;
    private double price;
    private List<String> excludedMemberNames;
    
}
