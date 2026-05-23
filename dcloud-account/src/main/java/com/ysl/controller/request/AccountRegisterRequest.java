package com.ysl.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "AccountRegisterRequest", description = "用户注册请求参数")
public class AccountRegisterRequest {
    @Schema(description = "用户头像地址，可选；不传则注册时不设置头像", example = "https://example.com/avatar.png")
    private String headImg;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "登录密码", example = "123456")
    private String pwd;

    @Schema(description = "邮箱", example = "test@example.com")
    private String mail;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

}
