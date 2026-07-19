package com.ysl.controller;

import com.ysl.controller.request.ShortLinkAddRequest;
import com.ysl.service.ShortLinkService;
import com.ysl.util.JsonData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/link/v1")
@RequiredArgsConstructor
@Tag(name = "短链模块", description = "短链创建、解析等接口")
public class ShortLinkController {
    private final ShortLinkService shortLinkService;


    @PostMapping("add")
    @Operation(summary = "创建短链", description = "提交原始链接、域名和分组信息，异步创建短链。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建提交结果", content = @Content(schema = @Schema(implementation = JsonData.class)))
    })
    public JsonData createShortLink(
            @Parameter(description = "创建短链请求参数", required = true) @RequestBody ShortLinkAddRequest request) {
        JsonData jsonData = shortLinkService.createShortLink(request);
        return jsonData;
    }

}
