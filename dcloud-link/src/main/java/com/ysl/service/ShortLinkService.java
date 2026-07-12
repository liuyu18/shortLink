package com.ysl.service;

import com.ysl.mapper.ShortLinkMapper;
import com.ysl.vo.ShortLinkVO;

public interface ShortLinkService {
    ShortLinkVO parseShortLink(String shortLinkCode);

}
