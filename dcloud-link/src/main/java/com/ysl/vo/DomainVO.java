package com.ysl.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class DomainVO implements Serializable {
    private Long id;
    private Long accountNo;
    private String domainType;
    private String value;
    private Integer del;
    private Date gmtCreate;
    private Date gmtModified;
}
