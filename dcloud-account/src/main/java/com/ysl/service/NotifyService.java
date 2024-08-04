package com.ysl.service;

import com.ysl.enums.SendCodeEnum;
import com.ysl.util.JsonData;

public interface NotifyService {
    JsonData sendCode(SendCodeEnum sendCodeEnum, String to);

    boolean checkCode(SendCodeEnum sendCodeEnum, String to, String code);
}