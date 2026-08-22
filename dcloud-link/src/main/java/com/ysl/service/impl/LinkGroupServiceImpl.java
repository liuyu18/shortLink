package com.ysl.service.impl;

import com.ysl.controller.request.LinkGroupAddRequest;
import com.ysl.controller.request.LinkGroupUpdateRequest;
import com.ysl.enums.BizCodeEnum;
import com.ysl.exception.BizException;
import com.ysl.interceptor.LoginInterceptor;
import com.ysl.manager.LinkGroupManager;
import com.ysl.model.LinkGroupDO;
import com.ysl.service.LinkGroupService;
import com.ysl.vo.LinkGroupVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class LinkGroupServiceImpl implements LinkGroupService {

    private final LinkGroupManager linkGroupManager;


    @Override
    public int add(LinkGroupAddRequest addRequest) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        if (linkGroupManager.existsByTitle(accountNo, addRequest.getTitle(), null)) {
            throw new BizException(BizCodeEnum.GROUP_REPEAT);
        }

        LinkGroupDO linkGroupDO = new LinkGroupDO();
        linkGroupDO.setTitle(addRequest.getTitle());
        linkGroupDO.setAccountNo(accountNo);
        try {
            return linkGroupManager.add(linkGroupDO);
        } catch (DuplicateKeyException e) {
            throw new BizException(BizCodeEnum.GROUP_REPEAT);
        }
    }

    @Override
    public int delete(Long groupId) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        return linkGroupManager.del(groupId, accountNo);
    }

    @Override
    public LinkGroupVO detail(Long groupId) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        LinkGroupDO linkGroupDO = linkGroupManager.detail(groupId, accountNo);
        LinkGroupVO linkGroupVO = new LinkGroupVO();
        BeanUtils.copyProperties(linkGroupDO, linkGroupVO);
        return linkGroupVO;
    }

    @Override
    public List<LinkGroupVO> listAllGroup() {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        List<LinkGroupDO> linkGroupDOList = linkGroupManager.listAllGroup(accountNo);
        return linkGroupDOList.stream().map(linkGroupDO -> {
            LinkGroupVO linkGroupVO = new LinkGroupVO();
            BeanUtils.copyProperties(linkGroupDO, linkGroupVO);
            return linkGroupVO;
        }).collect(Collectors.toList());
    }

    @Override
    public int updateById(LinkGroupUpdateRequest request) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        if (linkGroupManager.existsByTitle(accountNo, request.getTitle(), request.getId())) {
            throw new BizException(BizCodeEnum.GROUP_REPEAT);
        }

        LinkGroupDO linkGroupDO = new LinkGroupDO();
        linkGroupDO.setTitle(request.getTitle());
        linkGroupDO.setId(request.getId());
        linkGroupDO.setAccountNo(accountNo);

        try {
            return linkGroupManager.updateById(linkGroupDO);
        } catch (DuplicateKeyException e) {
            throw new BizException(BizCodeEnum.GROUP_REPEAT);
        }
    }
}
