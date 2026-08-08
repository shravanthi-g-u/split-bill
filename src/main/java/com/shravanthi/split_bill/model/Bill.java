package com.shravanthi.split_bill.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bill {
    
    private String id;
    private String name;
    private List<Item> items = new ArrayList<>();
    private List<Member> members = new ArrayList<>();
}