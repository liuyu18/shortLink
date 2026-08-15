package com.ysl.controller.request;

import lombok.Data;

@Data
public class ShortLinkUpdateRequest {

    private Long groupId;

    private Long mappingId;

    private String code;

    private String title;

    private Long domainId;

    private String domainType;
}
