package com.ysl.manager;

import com.ysl.model.ShortLinkDO;

public interface ShortLinkManager {
    int addShortLink(ShortLinkDO shortLinkDO);
    ShortLinkDO findByShortLinkCode(String shortLinkCode);
    int del(String shortLinkCode, Long accountNo);
}
