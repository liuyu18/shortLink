package com.ysl.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(name = "ShortLinkAddRequest", description = "创建短链请求参数")
public class ShortLinkAddRequest {

    /**
     * 组
     */
    @Schema(description = "短链分组 ID", example = "1")
    private Long groupId;

    /**
     * 短链标题
     */
    @Schema(description = "短链标题", example = "官网首页")
    private String title;

    /**
     * 原生url
     */
    @Schema(description = "原始访问地址", example = "https://www.example.com/activity?id=1001")
    private String originalUrl;

    /**
     * 域名id
     */
    @Schema(description = "域名 ID", example = "1")
    private Long domainId;

    /**
     * 域名类型
     */
    @Schema(description = "域名类型：OFFICIAL 官方域名，CUSTOM 自定义域名", example = "OFFICIAL", allowableValues = {"OFFICIAL", "CUSTOM"})
    private String domainType;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间，永久有效时可由业务约定为 -1 或不传", example = "2026-12-31T23:59:59.000+00:00")
    private Date expired;

}
