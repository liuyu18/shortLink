package com.ysl.service;

import com.ysl.controller.request.ShortLinkAddRequest;
import com.ysl.controller.request.ShortLinkDelRequest;
import com.ysl.controller.request.ShortLinkPageRequest;
import com.ysl.controller.request.ShortLinkUpdateRequest;
import com.ysl.mapper.ShortLinkMapper;
import com.ysl.model.EventMessage;
import com.ysl.util.JsonData;
import com.ysl.vo.ShortLinkVO;

import java.util.Map;

public interface ShortLinkService {
    ShortLinkVO parseShortLinkCode(String shortLinkCode);

    JsonData createShortLink(ShortLinkAddRequest request);


    boolean handleAddShortLink(EventMessage eventMessage);

    Map<String, Object> pageByGroupId(ShortLinkPageRequest request);

    JsonData del(ShortLinkDelRequest request);

    JsonData update(ShortLinkUpdateRequest request);


    boolean handleDelShortLink(EventMessage eventMessage);

    boolean handleUpdateShortLink(EventMessage eventMessage);

}
