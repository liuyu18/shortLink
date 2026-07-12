package com.ysl.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.*;
import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("damain")
public class DomainDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long accountNo;
    private String value;
    private Integer del;
    private Date gmtCreate;
    private Date gmtModified;

}
