package com.ysl.controller;

import com.google.code.kaptcha.Producer;
import com.ysl.controller.request.SendCodeRequest;
import com.ysl.enums.BizCodeEnum;
import com.ysl.enums.SendCodeEnum;
import com.ysl.service.NotifyService;
import com.ysl.util.CommonUtil;
import com.ysl.util.JsonData;
import com.ysl.util.LogUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/notify/v1")
@Slf4j
@Tag(name = "通知模块", description = "图形验证码、短信验证码等通知接口")
public class NotifyController {

    private final NotifyService notifyService;
    private final Producer captchaProducer;


    private StringRedisTemplate redisTemplate;

    private static final long CAPTCHA_CODE_EXPIRED = 1000 * 10 * 60;

    public NotifyController(NotifyService notifyService,
                            Producer captchaProducer,
                            StringRedisTemplate redisTemplate) {
        this.notifyService = notifyService;
        this.captchaProducer = captchaProducer;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping(value = "captcha", produces = MediaType.IMAGE_JPEG_VALUE)
    @Operation(summary = "获取图形验证码", description = "返回 JPG 图形验证码，并按客户端 IP 和 User-Agent 缓存在 Redis 中。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "验证码图片", content = @Content(mediaType = "image/jpeg"))
    })
    public void getCaptcha(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response) {
        String captchaText = captchaProducer.createText();
        LogUtil.info("验证码内容: {}", captchaText);
        redisTemplate.opsForValue().set(getCaptchaKey(request),
            captchaText,
            CAPTCHA_CODE_EXPIRED,
            TimeUnit.MILLISECONDS);

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

    private String getCaptchaKey(HttpServletRequest request) {
        String ip = CommonUtil.getIpAddr(request);
        String userAgent = request.getHeader("User-Agent");
        String key = "account-service:captcha:" + CommonUtil.MD5(ip + userAgent);

        return key;
    }

    @RequestMapping(value = "send_code", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "发送短信验证码", description = "校验图形验证码后，向指定手机号或邮箱发送注册验证码。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发送结果", content = @Content(schema = @Schema(implementation = JsonData.class)))
    })
    public JsonData sendCode(
            @Parameter(description = "发送验证码请求参数", required = true)
            @RequestBody SendCodeRequest sendCodeRequest,
            @Parameter(hidden = true)
            HttpServletRequest httpServletRequest) {
        String key = getCaptchaKey(httpServletRequest);
        String cacheCaptcha = redisTemplate.opsForValue().get(key);
        String captcha = sendCodeRequest.getCaptcha();

        if (captcha != null && cacheCaptcha != null && cacheCaptcha.equalsIgnoreCase(cacheCaptcha)) {
            redisTemplate.delete(key);
            JsonData jsonData = notifyService.sendCode(SendCodeEnum.USER_REGISTER,
                sendCodeRequest.getTo());
            return jsonData;
        } else {
            return JsonData.buildResult(BizCodeEnum.CODE_CAPTCHA_ERROR);
        }

    }

}
