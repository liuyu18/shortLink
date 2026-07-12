package com.ysl.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("group_code_mapping")
public class GroupCodeMappingDO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long groupId;
    private String title;
    private String originalUrl;
    private String domain;
    private String code;
    private String sign;
    private Date expired;
    private Long accountNo;
    private Date gmtCreate;
    private Date gmtModified;
    private Integer del;
    private String state;
    private String linkType;

}
