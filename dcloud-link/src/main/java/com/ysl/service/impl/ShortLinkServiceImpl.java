package com.ysl.service.impl;

import com.ysl.manager.ShortLinkManager;
import com.ysl.mapper.ShortLinkMapper;
import com.ysl.model.ShortLinkDO;
import com.ysl.service.ShortLinkService;
import com.ysl.vo.ShortLinkVO;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl implements ShortLinkService {

    private final ShortLinkManager shortLinkManager;

    @Override
    public ShortLinkVO parseShortLink(String shortLinkCode) {
        ShortLinkDO shortLinkDO = shortLinkManager.findByShortLinkCode(shortLinkCode);
        if (shortLinkDO == null) {
            return null;
        }
        ShortLinkVO shortLinkVO = new ShortLinkVO();
        BeanUtils.copyProperties(shortLinkDO, shortLinkVO);
        return shortLinkVO;
    }
}
