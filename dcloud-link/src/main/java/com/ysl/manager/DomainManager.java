package com.ysl.manager;

import com.ysl.enums.DomainTypeEnum;
import com.ysl.model.DomainDO;
import org.apache.http.conn.util.DomainType;

import java.util.List;

public interface DomainManager {

    DomainDO findById(Long id, Long accountNo);
    DomainDO findByDomainTypeAndID(Long id, DomainTypeEnum  domainTypeEnum);
    int addDomain(DomainDO domainDO);
    List<DomainDO> listOfficialDomain();
    List<DomainDO> listCustomDomain(Long accountNo);
}
