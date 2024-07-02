package com.ysl.service.impl;

import com.ysl.component.SmsComponent;
import com.ysl.config.SmsConfig;
import com.ysl.enums.SendCodeEnum;
import com.ysl.service.NotifyService;
import com.ysl.util.CheckUtil;
import com.ysl.util.CommonUtil;
import com.ysl.util.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.smartcardio.CommandAPDU;

@Service
@Slf4j
public class NotifyServiceImpl implements NotifyService {
    private final RestTemplate restTemplate;
    private final SmsComponent smsComponent;
    private final SmsConfig smsConfig;
    @Autowired
    public NotifyServiceImpl(RestTemplate restTemplate, SmsComponent smsComponent, SmsConfig smsConfig) {
        this.restTemplate = restTemplate;
        this.smsComponent = smsComponent;
        this.smsConfig = smsConfig;
    }

    @Override
    public JsonData sendCode(SendCodeEnum sendCodeEnum, String to) {
        String code = CommonUtil.getRandomCode(6);
        if (CheckUtil.isEmail(to)) {

        }else if(CheckUtil.isPhone(to)){
        smsComponent.send(to, smsConfig.getTemplateId(), code);
        }
        return JsonData.buildSuccess();
    }
}