package com.ysl.exception;

import com.ysl.enums.BizCodeEnum;

import lombok.Data;

@Data
public class BizException extends RuntimeException {
    private int code;
    private String msg;

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
        this.msg = msg;
    }

    public BizException(BizCodeEnum bizCodeEnum) {
        super(bizCodeEnum.getMessage());
        this.code = bizCodeEnum.getCode();
        this.msg = bizCodeEnum.getMessage();
    }
}
