package com.ysl.service;

import com.ysl.controller.request.ShortLinkAddRequest;
import com.ysl.controller.request.ShortLinkPageRequest;
import com.ysl.mapper.ShortLinkMapper;
import com.ysl.model.EventMessage;
import com.ysl.util.JsonData;
import com.ysl.vo.ShortLinkVO;

import java.util.Map;

public interface ShortLinkService {
    ShortLinkVO parseShortLinkCode(String shortLinkCode);

    JsonData createShortLink(ShortLinkAddRequest request);


    boolean handlerAddShortLink(EventMessage eventMessage);

    Map<String, Object> pageByGroupId(ShortLinkPageRequest request);

}
