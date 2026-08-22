package com.ysl.manager;

import com.ysl.model.LinkGroupDO;

import java.util.List;

public interface LinkGroupManager {
    int add(LinkGroupDO linkGroupDO);

    boolean existsByTitle(Long accountNo, String title, Long excludeGroupId);

    int del(Long groupId, Long accountNo);

    LinkGroupDO detail(Long groupId, Long accountNo);

    List<LinkGroupDO> listAllGroup(Long accountNo);

    int updateById(LinkGroupDO linkGroupDO);
}
