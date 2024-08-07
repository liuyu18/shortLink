package com.ysl.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ysl.enums.AuthTypeEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author ryan
 * @since 2024-06-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("account")
public class AccountDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long accountNo;


    private String headImg;

   
    private String phone;

    private String pwd;


    private String secret;


    private String mail;

    private String username;

    private String auth;

    private Date gmtCreate;

    private Date gmtModified;


}
