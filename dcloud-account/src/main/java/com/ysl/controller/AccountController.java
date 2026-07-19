package com.ysl.controller;

import io.swagger.v3.core.util.Json;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import com.ysl.controller.request.AccountLoginRequest;
import com.ysl.controller.request.AccountRegisterRequest;
import com.ysl.enums.BizCodeEnum;
import com.ysl.service.AccountService;
import com.ysl.service.FileService;
import com.ysl.util.JsonData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 *
 * @author ryan
 * @since 2024-06-16
 */
@RestController
@RequestMapping("/api/account/v1")
@Tag(name = "账号模块", description = "用户账号注册、头像上传等接口")
public class AccountController {

    private final FileService fileService;
    private final AccountService accountService;

    AccountController(FileService fileService, AccountService accountService) {
        this.fileService = fileService;
        this.accountService = accountService;
    }

    @PostMapping(value = "login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "用户登录", description = "使用手机号和密码登录，成功后返回 token。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录结果", content = @Content(schema = @Schema(implementation = JsonData.class)))
    })
    public JsonData login(
            @Parameter(description = "登录请求参数", required = true) @RequestBody AccountLoginRequest request) {
        JsonData jsonData = accountService.login(request);
        return jsonData;
    }

    @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传用户头像", description = "上传头像图片，成功后返回图片访问地址。请求类型为 multipart/form-data。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "上传成功或业务失败，统一返回 JsonData", content = @Content(schema = @Schema(implementation = JsonData.class)))
    })
    public JsonData uploadUserImg(
            @Parameter(description = "头像文件", required = true, schema = @Schema(type = "string", format = "binary")) @RequestPart("file") MultipartFile file) {

        String result = fileService.uploadUserImg(file);

        return result != null ? JsonData.buildSuccess(result)
                : JsonData.buildResult(BizCodeEnum.FILE_UPLOAD_USER_IMG_FAIL);

    }

    @PostMapping(value = "register", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "用户注册", description = "使用手机号、邮箱、用户名和密码注册账号，头像为可选参数。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "注册结果", content = @Content(schema = @Schema(implementation = JsonData.class)))
    })
    public JsonData register(
            @Parameter(description = "注册请求参数", required = true) @RequestBody AccountRegisterRequest registerRequest) {
        JsonData jsonData = accountService.register(registerRequest);
        return jsonData;
    }
}
