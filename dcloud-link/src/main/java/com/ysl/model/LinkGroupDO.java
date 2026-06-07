package com.ysl.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("link_group")
public class LinkGroupDO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String title;
    private Long accountNo;
    private Date gmtCreate;
    private Date gmtModified;

}
