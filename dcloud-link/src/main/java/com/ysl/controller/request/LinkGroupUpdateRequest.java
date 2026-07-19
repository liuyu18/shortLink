package com.ysl.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "LinkGroupUpdateRequest", description = "更新短链分组请求参数")
public class LinkGroupUpdateRequest {
    @Schema(description = "分组 ID", example = "1")
    private Long id;

    @Schema(description = "分组名称", example = "活动分组")
    private String title;
}
