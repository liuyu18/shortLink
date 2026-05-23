package com.ysl.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "AccountLoginRequest", description = "用户登录请求参数")
public class AccountLoginRequest {
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "登录密码", example = "123456")
    private String pwd;

}
