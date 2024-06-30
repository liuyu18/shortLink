package com.ysl.controller;

import com.google.code.kaptcha.Producer;
import com.ysl.service.NotifyService;
import com.ysl.util.JsonData;
import com.ysl.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

@RestController
@RequestMapping("/api/account/v1")
@Slf4j
public class NotifyController {
    
    private final NotifyService notifyService;
    private final Producer captchaProducer;
    
    private StringRedisTemplate stringRedisTemplate;

    private RedisTemplate redisTemplate;

    @Autowired
    public NotifyController(NotifyService notifyService, Producer captchaProducer) {
        this.notifyService = notifyService;
        this.captchaProducer = captchaProducer;
    }
    
    @GetMapping("captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        String captchaText = captchaProducer.createText();
        LogUtil.info("验证码内容: {}", captchaText);
        BufferedImage bufferedImage = captchaProducer.createImage(captchaText);
        
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(bufferedImage, "jpg", outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            LogUtil.error("获取流出错: {}", e.getMessage());
        }
    }
    
    @RequestMapping("send_code")
    public JsonData sendCode(){
        return JsonData.buildSuccess();
    }

}