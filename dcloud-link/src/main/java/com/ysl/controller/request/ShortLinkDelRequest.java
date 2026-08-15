package com.ysl.controller.request;

import lombok.Data;

@Data
public class ShortLinkDelRequest {


    private Long groupId;


    private Long mappingId;


    private String code;
}
