package com.ysl.manager;

import com.ysl.enums.ShortLinkStateEnum;
import com.ysl.model.GroupCodeMappingDO;

import java.util.Map;

public interface GroupCodeMappingManager {
    GroupCodeMappingDO findByGroupIdAndMappingId(Long mappingId,Long accountNo,Long groupId);
    int add(GroupCodeMappingDO groupCodeMappingDO);
    int del(String shortLinkCode, Long accountNo, Long groupId);
    Map<String, Object> pageShortLinkByGroupId(Integer page, Integer size, Long account, Long groupId);
    int updateGroupCodeMappingState(Long accountNo, Long groupId, String shortLinkCode, ShortLinkStateEnum shortLinkStateEnum);

}
