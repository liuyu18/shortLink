package com.ysl.service.impl;

import com.ysl.interceptor.LoginInterceptor;
import com.ysl.manager.DomainManager;
import com.ysl.model.DomainDO;
import com.ysl.service.DomainService;
import com.ysl.vo.DomainVO;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DomainServiceImpl implements DomainService {


    private final DomainManager domainManager;

    @Override
    public List<DomainVO> listAll() {

        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();

        List<DomainDO> custDomainList = domainManager.listCustomDomain(accountNo);
        List<DomainDO> officialDomainList = domainManager.listOfficialDomain();

        custDomainList.addAll(officialDomainList);


        return custDomainList.stream().map(obj -> beanProcess(obj)).collect(Collectors.toList());
    }

    private DomainVO beanProcess(DomainDO domainDO) {
        DomainVO domainVO = new DomainVO();
        BeanUtils.copyProperties(domainDO, domainVO);
        return domainVO;
    }
}
