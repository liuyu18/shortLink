package com.ysl.controller.request;

import lombok.Data;

@Data
public class AccountRegisterRequest {
    private String headImg;
    private String phone;
    private String pwd;
    private String mail;
    private String username;
    private String code;
}
