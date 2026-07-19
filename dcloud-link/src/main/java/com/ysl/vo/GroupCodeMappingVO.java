package com.ysl.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "GroupCodeMappingVO", description = "短链分组和短链码映射信息")
public class GroupCodeMappingVO implements Serializable {

    @Schema(description = "映射 ID", example = "1")
    private Long id;


    @Schema(description = "短链分组 ID", example = "1")
    private Long groupId;


    @Schema(description = "短链标题", example = "官网首页")
    private String title;


    @Schema(description = "原始访问地址", example = "https://www.example.com/activity?id=1001")
    private String originalUrl;


    @Schema(description = "短链域名", example = "s.example.com")
    private String domain;


    @Schema(description = "短链码", example = "AbCdEf")
    private String code;


    @Schema(description = "原始链接签名", example = "e10adc3949ba59abbe56e057f20f883e")
    private String sign;


    @Schema(description = "过期时间", example = "2026-12-31T23:59:59.000+00:00")
    private Date expired;


    @Schema(description = "账号编号", example = "1000001")
    private Long accountNo;


    @Schema(description = "创建时间", example = "2026-07-19T10:00:00.000+00:00")
    private Date gmtCreate;


    @Schema(description = "更新时间", example = "2026-07-19T10:30:00.000+00:00")
    private Date gmtModified;


    @Schema(description = "删除标识，0 未删除，1 已删除", example = "0")
    private Integer del;


    @Schema(description = "短链状态：ACTIVE 启用，LOCK 锁定", example = "ACTIVE", allowableValues = {"ACTIVE", "LOCK"})
    private String state;

    @Schema(description = "短链类型", example = "OFFICIAL")
    private String linkType;

}
