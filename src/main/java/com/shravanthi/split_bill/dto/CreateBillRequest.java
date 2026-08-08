package com.shravanthi.split_bill.dto;

import java.util.List;

import lombok.Data;

@Data
public class CreateBillRequest {
    private String name;
    private List<String> memberNames;
    
}
