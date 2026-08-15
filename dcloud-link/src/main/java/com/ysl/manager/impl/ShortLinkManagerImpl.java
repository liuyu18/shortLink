package com.ysl.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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

        return shortLinkMapper.selectOne(
                new QueryWrapper<ShortLinkDO>().eq("code", shortLinkCode));
    }

    @Override
    public int del(ShortLinkDO shortLinkDO) {
        int rows = shortLinkMapper.update(null,
                new UpdateWrapper<ShortLinkDO>()
                        .eq("code", shortLinkDO.getCode())
                        .eq("account_no", shortLinkDO.getAccountNo())
                        .set("del", 1));
        return rows;
    }

    @Override
    public int update(ShortLinkDO shortLinkDO) {
        int rows = shortLinkMapper.update(null, new UpdateWrapper<ShortLinkDO>()
                .eq("code", shortLinkDO.getCode())
                .eq("del", 0)
                .eq("account_no", shortLinkDO.getAccountNo())

                .set("title", shortLinkDO.getTitle())
                .set("domain", shortLinkDO.getDomain()));


        return rows;
    }
    
}
