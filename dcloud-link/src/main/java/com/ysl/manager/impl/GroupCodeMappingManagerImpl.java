package com.ysl.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ysl.enums.ShortLinkStateEnum;
import com.ysl.manager.GroupCodeMappingManager;
import com.ysl.mapper.GroupCodeMappingMapper;
import com.ysl.model.GroupCodeMappingDO;
import com.ysl.vo.GroupCodeMappingVO;
import groovy.util.logging.Slf4j;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@Component
@Slf4j
@RequiredArgsConstructor
public class GroupCodeMappingManagerImpl implements GroupCodeMappingManager {


    private final GroupCodeMappingMapper groupCodeMappingMapper;


    @Override
    public GroupCodeMappingDO findByGroupIdAndMappingId(Long mappingId, Long accountNo, Long groupId) {
        GroupCodeMappingDO groupCodeMappingDO = groupCodeMappingMapper.selectOne(
                new QueryWrapper<GroupCodeMappingDO>()
                        .eq("mapping_id", mappingId)
                        .eq("account_no", accountNo)
                        .eq("group_id", groupId)
        );
        return groupCodeMappingDO;
    }

    @Override
    public int add(GroupCodeMappingDO groupCodeMappingDO) {
        return groupCodeMappingMapper.insert(groupCodeMappingDO);
    }

    @Override
    public int del(String shortLinkCode, Long accountNo, Long groupId) {
        int rows = groupCodeMappingMapper.update(
                null,
                new UpdateWrapper<GroupCodeMappingDO>()
                        .eq("code", shortLinkCode)
                .eq("account_no", accountNo)
                .eq("group_id", groupId)
                        .set("del", 1)
        );
        return rows;
    }

    @Override
    public Map<String, Object> pageShortLinkByGroupId(Integer page, Integer size, Long accountNo, Long groupId) {
        Page<GroupCodeMappingDO> pageInfo = new Page<>(page, size);

        Page<GroupCodeMappingDO> groupCodeMappingDOPage = groupCodeMappingMapper.selectPage(
                pageInfo,
                new QueryWrapper<GroupCodeMappingDO>()
                        .eq("account_no", accountNo)
                        .eq("group_id", groupId)
        );
        Map<String, Object> pageMap = new HashMap<>(3);
        pageMap.put("total_record", groupCodeMappingDOPage.getTotal());
        pageMap.put("total_page", groupCodeMappingDOPage.getRecords());
        pageMap.put("current_data", groupCodeMappingDOPage.getRecords()
                .stream().map(obj -> beanProcess(obj)).collect(Collectors.toList()));
        return Map.of();
    }

    @Override
    public int updateGroupCodeMappingState(Long accountNo, Long groupId, String shortLinkCode, ShortLinkStateEnum shortLinkStateEnum) {
        int rows = groupCodeMappingMapper.update(null,
                new UpdateWrapper<GroupCodeMappingDO>()
                        .eq("code", shortLinkCode)
                        .eq("account_no", accountNo)
                        .eq("group_id", groupId)
                        .set("state", shortLinkStateEnum.name()));

        return rows;
    }

    private GroupCodeMappingVO beanProcess(GroupCodeMappingDO groupCodeMappingDO) {

        GroupCodeMappingVO groupCodeMappingVO = new GroupCodeMappingVO();
        BeanUtils.copyProperties(groupCodeMappingDO, groupCodeMappingVO);
        return groupCodeMappingVO;

    }
}
