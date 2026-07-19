package com.ysl.service;

import com.ysl.controller.request.ShortLinkAddRequest;
import com.ysl.mapper.ShortLinkMapper;
import com.ysl.util.JsonData;
import com.ysl.vo.ShortLinkVO;

public interface ShortLinkService {
    ShortLinkVO parseShortLink(String shortLinkCode);

    JsonData createShortLink(ShortLinkAddRequest request);

}
