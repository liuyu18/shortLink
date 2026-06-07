package com.ysl.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class LinkGroupVO implements Serializable {
    private Long id;
    private String title;
    private Long accountNo;

    private Date gmtCreate;

    private Date gmtModified;
}
