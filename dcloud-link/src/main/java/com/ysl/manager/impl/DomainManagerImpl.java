package com.ysl.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ysl.enums.DomainTypeEnum;
import com.ysl.manager.DomainManager;
import com.ysl.mapper.DomainMapper;
import com.ysl.model.DomainDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DomainManagerImpl implements DomainManager {

    private final DomainMapper domainMapper;

    @Override
    public DomainDO findById(Long id, Long accountNo) {
        return domainMapper.selectOne(
                new QueryWrapper<DomainDO>()
                        .eq("id", id)
                        .eq("account_no", accountNo)
        );
    }

    @Override
    public DomainDO findByDomainTypeAndID(Long id, DomainTypeEnum domainTypeEnum) {
        return domainMapper
                .selectOne(
                        new QueryWrapper<DomainDO>()
                                .eq("id", id)
                                .eq("domain_type", domainTypeEnum.name()
                                )
                );
    }

    @Override
    public int addDomain(DomainDO domainDO) {
        return domainMapper.insert(domainDO);
    }

    @Override
    public List<DomainDO> listOfficialDomain() {
        return domainMapper.selectList(
                new QueryWrapper<DomainDO>()
                        .eq("domain_type", DomainTypeEnum.CUSTOM.name()
                        )
        );
    }

    @Override
    public List<DomainDO> listCustomDomain(Long accountNo) {
        return domainMapper.selectList(
                new QueryWrapper<DomainDO>()
                        .eq("domain_type", DomainTypeEnum.CUSTOM.name())
                        .eq("account_no", accountNo)
        );
    }
}
