package com.shravanthi.split_bill.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    private String name;
    private double price;
    private Set<Member> excludedMembers = new HashSet<>(); // default: nobody excluded
}