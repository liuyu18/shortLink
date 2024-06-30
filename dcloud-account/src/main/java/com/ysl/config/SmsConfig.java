package com.ysl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "sms")
@Configuration
@Data
public class SmsConfig {
    private String templateId;
    private String appCode;
    private String smsSignId;
}