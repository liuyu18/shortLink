package com.ysl.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "SendCodeRequest", description = "发送验证码请求参数")
public class SendCodeRequest {
    @Schema(description = "图形验证码内容", example = "a8x3")
    private String captcha;

    @Schema(description = "接收验证码的手机号或邮箱", example = "13800138000")
    private String to;
}
