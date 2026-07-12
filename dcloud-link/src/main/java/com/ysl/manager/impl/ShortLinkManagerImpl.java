package com.ysl.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ysl.manager.ShortLinkManager;
import com.ysl.mapper.ShortLinkMapper;
import com.ysl.model.ShortLinkDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ShortLinkManagerImpl implements ShortLinkManager {

    private final ShortLinkMapper shortLinkMapper;

    public int addShortLink(ShortLinkDO shortLinkDO) {
        return shortLinkMapper.insert(shortLinkDO);
    }

    @Override
    public ShortLinkDO findByShortLinkCode(String shortLinkCode) {

        ShortLinkDO shortLinkDO = shortLinkMapper.selectOne(
                new QueryWrapper<ShortLinkDO>().eq("code", shortLinkCode));
        return shortLinkDO;
    }

    public int del(String shortLinkCode, Long accountNo) {

        ShortLinkDO shortLinkDO = new ShortLinkDO();
        shortLinkDO.setDel(1);

        int rows = shortLinkMapper.update(shortLinkDO,
                new QueryWrapper<ShortLinkDO>().eq("code", shortLinkCode).eq("account_no", accountNo));
        return rows;
    }
}
