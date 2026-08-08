package com.shravanthi.split_bill.dto;

import java.util.List;

import lombok.Data;

@Data
public class CreateBillRequest {

    private List<String> memberNames;
    
}
