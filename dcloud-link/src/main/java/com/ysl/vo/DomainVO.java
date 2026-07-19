package com.ysl.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DomainVO", description = "短链域名信息")
public class DomainVO implements Serializable {
    @Schema(description = "域名 ID", example = "1")
    private Long id;

    @Schema(description = "账号编号", example = "1000001")
    private Long accountNo;

    @Schema(description = "域名类型：OFFICIAL 官方域名，CUSTOM 自定义域名", example = "OFFICIAL", allowableValues = {"OFFICIAL", "CUSTOM"})
    private String domainType;

    @Schema(description = "域名值", example = "s.example.com")
    private String value;

    @Schema(description = "删除标识，0 未删除，1 已删除", example = "0")
    private Integer del;

    @Schema(description = "创建时间", example = "2026-07-19T10:00:00.000+00:00")
    private Date gmtCreate;

    @Schema(description = "更新时间", example = "2026-07-19T10:30:00.000+00:00")
    private Date gmtModified;
}
