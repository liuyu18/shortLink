package com.ysl.service.impl;

import com.ysl.component.ShortLinkComponent;
import com.ysl.config.RabbitMQConfig;
import com.ysl.controller.request.ShortLinkAddRequest;
import com.ysl.enums.DomainTypeEnum;
import com.ysl.enums.EventMessageType;
import com.ysl.enums.ShortLinkStateEnum;
import com.ysl.interceptor.LoginInterceptor;
import com.ysl.manager.DomainManager;
import com.ysl.manager.GroupCodeMappingManager;
import com.ysl.manager.LinkGroupManager;
import com.ysl.manager.ShortLinkManager;
import com.ysl.model.*;
import com.ysl.service.ShortLinkService;
import com.ysl.util.CommonUtil;
import com.ysl.util.IDUtil;
import com.ysl.util.JsonData;
import com.ysl.util.JsonUtil;
import com.ysl.vo.ShortLinkVO;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl implements ShortLinkService {

    private final ShortLinkManager shortLinkManager;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQConfig rabbitMQConfig;
    private final DomainManager domainManager;
    private final LinkGroupManager linkGroupManager;
    private final ShortLinkComponent shortLinkComponent;
    private final GroupCodeMappingManager groupCodeMappingManager;

    @Override
    public ShortLinkVO parseShortLink(String shortLinkCode) {
        ShortLinkDO shortLinkDO = shortLinkManager.findByShortLinkCode(shortLinkCode);
        if (shortLinkDO == null) {
            return null;
        }
        ShortLinkVO shortLinkVO = new ShortLinkVO();
        BeanUtils.copyProperties(shortLinkDO, shortLinkVO);
        return shortLinkVO;
    }

    @Override
    public JsonData createShortLink(ShortLinkAddRequest request) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        EventMessage eventMessage = EventMessage.builder().accountNo(accountNo).
                content(JsonUtil.obj2Json(request))
                .messageId(IDUtil.geneSnowFlakeID().toString())
                .eventMessageType(EventMessageType.SHORT_LINK_ADD.name())
                .build();
        rabbitTemplate.convertAndSend(
                rabbitMQConfig.getShortLinkEventExchange(),
                rabbitMQConfig.getShortLinkAddRoutingKey(),
                eventMessage
        );
        return JsonData.buildSuccess();
    }

    @Override
    public boolean handlerAddShortLink(EventMessage eventMessage) {


        Long accountNo = eventMessage.getAccountNo();
        String messageType = eventMessage.getEventMessageType();
        ShortLinkAddRequest addRequest = JsonUtil.json2Obj(eventMessage.getContent(), ShortLinkAddRequest.class);
        DomainDO domainDO = checkDomain(addRequest.getDomainType(), addRequest.getDomainId(), accountNo);
        LinkGroupDO linkGroupDO = checkLinkGroup(addRequest.getGroupId(), accountNo);
        String originalUrlDigest = CommonUtil.MD5(addRequest.getOriginalUrl());
        String shortLinkCode = shortLinkComponent.createShortLinkCode(addRequest.getOriginalUrl());
        ShortLinkDO ShortLinCodeDOInDB = shortLinkManager.findByShortLinkCode(shortLinkCode);
        if (ShortLinCodeDOInDB == null) {
            if (EventMessageType.SHORT_LINK_ADD_LINK.name().equalsIgnoreCase(messageType)) {
                ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                        .accountNo(accountNo)
                        .code(shortLinkCode)
                        .title(addRequest.getTitle())
                        .originalUrl(addRequest.getOriginalUrl())
                        .domain(domainDO.getValue())
                        .groupId(linkGroupDO.getId())
                        .expired(addRequest.getExpired())
                        .sign(originalUrlDigest)
                        .state(ShortLinkStateEnum.ACTIVE.name())
                        .del(0)
                        .build();
                shortLinkManager.addShortLink(shortLinkDO);
                return true;
            } else if (EventMessageType.SHORT_LINK_ADD_MAPPING.name().equalsIgnoreCase(messageType)) {
                GroupCodeMappingDO groupCodeMappingDO = GroupCodeMappingDO.builder()
                        .accountNo(accountNo)
                        .code(shortLinkCode)
                        .title(addRequest.getTitle())
                        .originalUrl(addRequest.getOriginalUrl())
                        .domain(domainDO.getValue())
                        .groupId(linkGroupDO.getId())
                        .expired(addRequest.getExpired())
                        .sign(originalUrlDigest)
                        .state(ShortLinkStateEnum.ACTIVE.name())
                        .del(0)
                        .build();

                groupCodeMappingManager.add(groupCodeMappingDO);
                return true;

            }
        }
        return false;

    }


    private DomainDO checkDomain(String domainType, Long domainId, Long accountNo) {
        DomainDO domainDO;
        if (DomainTypeEnum.CUSTOM.name().equalsIgnoreCase(domainType)) {
            domainDO = domainManager.findById(domainId, accountNo);
        } else {
            domainDO = domainManager.findByDomainTypeAndID(domainId, DomainTypeEnum.OFFICIAL);
        }
        Assert.notNull(domainDO, "短链域名不合法");
        return domainDO;

    }

    private LinkGroupDO checkLinkGroup(Long groupId, Long accountNo) {
        LinkGroupDO linkGroupDO = linkGroupManager.detail(groupId, accountNo);
        Assert.notNull(linkGroupDO, "组名不合法");
        return linkGroupDO;
    }

}
