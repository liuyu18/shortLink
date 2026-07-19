package com.ysl.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "LinkGroupVO", description = "短链分组信息")
public class LinkGroupVO implements Serializable {
    @Schema(description = "分组 ID", example = "1")
    private Long id;

    @Schema(description = "分组名称", example = "默认分组")
    private String title;

    @Schema(description = "账号编号", example = "1000001")
    private Long accountNo;

    @Schema(description = "创建时间", example = "2026-07-19T10:00:00.000+00:00")
    private Date gmtCreate;

    @Schema(description = "更新时间", example = "2026-07-19T10:30:00.000+00:00")
    private Date gmtModified;
}
