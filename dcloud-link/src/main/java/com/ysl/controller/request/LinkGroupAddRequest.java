package com.ysl.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "LinkGroupAddRequest", description = "新增短链分组请求参数")
public class LinkGroupAddRequest {
    @Schema(description = "分组名称", example = "默认分组")
    private String title;
}
