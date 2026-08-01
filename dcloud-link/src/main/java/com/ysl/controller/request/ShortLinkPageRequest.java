package com.ysl.controller.request;

import lombok.Data;

@Data
public class ShortLinkPageRequest {

    private Long groupId;
    private int page;
    private int size;
}
