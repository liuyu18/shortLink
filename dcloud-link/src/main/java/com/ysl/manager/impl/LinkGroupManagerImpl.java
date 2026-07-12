package com.ysl.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ysl.manager.LinkGroupManager;
import com.ysl.mapper.LinkGroupMapper;
import com.ysl.model.LinkGroupDO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LinkGroupManagerImpl implements LinkGroupManager {
    private final LinkGroupMapper linkGroupMapper;

    @Override
    public int add(LinkGroupDO linkGroupDO) {
        return linkGroupMapper.insert(linkGroupDO);
    }

    @Override
    public int del(Long groupId, Long accountNo) {
        return linkGroupMapper.delete(new QueryWrapper<LinkGroupDO>().eq("id",groupId).eq("account_no",accountNo));
    }

    @Override
    public LinkGroupDO detail(Long groupId, Long accountNo) {
        return linkGroupMapper.selectOne(new QueryWrapper<LinkGroupDO>().eq("id",groupId).eq("account_no",accountNo));
    }

    @Override
    public List<LinkGroupDO> listAllGroup(Long accountNo) {
        return linkGroupMapper.selectList(new QueryWrapper<LinkGroupDO>().eq("account_no",accountNo));
    }

    @Override
    public int updateById(LinkGroupDO linkGroupDO) {
        return linkGroupMapper.update(linkGroupDO,new QueryWrapper<LinkGroupDO>().eq("id",linkGroupDO.getId()).eq("account_no",linkGroupDO.getAccountNo()));

    }
}
