package com.ysl.service.impl;

import com.ysl.component.SmsComponent;
import com.ysl.config.SmsConfig;
import com.ysl.constant.RedisKey;
import com.ysl.enums.BizCodeEnum;
import com.ysl.enums.SendCodeEnum;
import com.ysl.service.NotifyService;
import com.ysl.util.CheckUtil;
import com.ysl.util.CommonUtil;
import com.ysl.util.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

@Service
@Slf4j
public class NotifyServiceImpl implements NotifyService {
    private static final int CODE_EXPIRED = 60 * 1000 * 10;
    private final RestTemplate restTemplate;
    private final SmsComponent smsComponent;
    private final SmsConfig smsConfig;
    private StringRedisTemplate redisTemplate;

    public NotifyServiceImpl(RestTemplate restTemplate,
            SmsComponent smsComponent,
            StringRedisTemplate redisTemplate,
            SmsConfig smsConfig) {
        this.restTemplate = restTemplate;
        this.smsComponent = smsComponent;
        this.smsConfig = smsConfig;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public JsonData sendCode(SendCodeEnum sendCodeEnum, String to) {
        String cacheKey = String.format(RedisKey.CHECK_CODE_KEY, sendCodeEnum.name(), to);
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.isNotBlank(cacheValue)) {
            long ttl = Long.parseLong(cacheValue.split("_")[1]);
            long leftTime = CommonUtil.getCurrentTimestamp() - ttl;
            if (leftTime < (1000 * 60)) {
                log.info("重复发送短信验证码，时间间隔:{}秒", leftTime);
                return JsonData.buildResult(BizCodeEnum.CODE_LIMITED);
            }
        }
        String code = CommonUtil.getRandomCode(6);
        String value = code + "_" + CommonUtil.getCurrentTimestamp();
        redisTemplate.opsForValue().set(cacheKey, value, CODE_EXPIRED, TimeUnit.MILLISECONDS);
        if (CheckUtil.isEmail(to)) {

        } else if (CheckUtil.isPhone(to)) {
            smsComponent.send(to, smsConfig.getTemplateId(), code);
        }
        return JsonData.buildSuccess();
    }

    public boolean checkCode(SendCodeEnum sendCodeEnum, String to, String code) {
        String cacheKey = String.format(RedisKey.CHECK_CODE_KEY, sendCodeEnum.name(), to);
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);

        if (StringUtils.isNotBlank(cacheValue)) {
            String cacheCode = cacheValue.split("_")[0];
            if (cacheCode.equalsIgnoreCase(code)) {
                redisTemplate.delete(code);
                return true;
            }
        }
        return false;

    }
}