package com.ysl.model;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginUser {
    private Long accountNo;
    private String username;
    private String headImg;
    private String mail;
    private String phone;
    private String auth;
}
