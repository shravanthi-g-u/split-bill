package com.shravanthi.split_bill.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String username;
    private String token; // null for plain registration responses, populated after login
}