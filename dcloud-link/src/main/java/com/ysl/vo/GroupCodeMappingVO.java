package com.ysl.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class GroupCodeMappingVO implements Serializable {

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
