package com.ysl.service;

import com.ysl.controller.request.LinkGroupAddRequest;
import com.ysl.controller.request.LinkGroupUpdateRequest;
import com.ysl.vo.LinkGroupVO;

import java.util.List;

public interface LinkGroupService {
    int add(LinkGroupAddRequest addRequest);
    int delete(Long groupId);
    LinkGroupVO detail(Long groupId);
    List<LinkGroupVO> listAllGroup();
    int updateById(LinkGroupUpdateRequest request);

}
