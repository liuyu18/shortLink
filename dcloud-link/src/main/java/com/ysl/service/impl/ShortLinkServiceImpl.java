package com.ysl.service.impl;

import com.ysl.component.ShortLinkComponent;
import com.ysl.config.RabbitMQConfig;
import com.ysl.controller.request.ShortLinkAddRequest;
import com.ysl.controller.request.ShortLinkDelRequest;
import com.ysl.controller.request.ShortLinkPageRequest;
import com.ysl.controller.request.ShortLinkUpdateRequest;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.Assert;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;


import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;


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
    private final RedisTemplate<Object, Object> redisTemplate;

    @Override
    public ShortLinkVO parseShortLinkCode(String shortLinkCode) {
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
        String newOriginalUrl = CommonUtil.addUrlPrefix(request.getOriginalUrl());
        request.setOriginalUrl(newOriginalUrl);

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
    public boolean handleAddShortLink(EventMessage eventMessage) {

        Long accountNo = eventMessage.getAccountNo();
        String messageType = eventMessage.getEventMessageType();

        ShortLinkAddRequest addRequest = JsonUtil.json2Obj(eventMessage.getContent(), ShortLinkAddRequest.class);
        //短链域名校验
        DomainDO domainDO = checkDomain(addRequest.getDomainType(), addRequest.getDomainId(), accountNo);
        //校验组是否合法
        LinkGroupDO linkGroupDO = checkLinkGroup(addRequest.getGroupId(), accountNo);

        //长链摘要
        String originalUrlDigest = CommonUtil.MD5(addRequest.getOriginalUrl());

        //短链码重复标记
        boolean duplicateCodeFlag = false;

        //生成短链码
        String shortLinkCode = shortLinkComponent.createShortLinkCode(addRequest.getOriginalUrl());

        //加锁
        //key1是短链码，ARGV[1]是accountNo,ARGV[2]是过期时间
        String script = "if redis.call('EXISTS',KEYS[1])==0 then redis.call('set',KEYS[1],ARGV[1]); redis.call('expire',KEYS[1],ARGV[2]); return 1;" +
                " elseif redis.call('get',KEYS[1]) == ARGV[1] then return 2;" +
                " else return 0; end;";

        Long result = redisTemplate.execute(new
                DefaultRedisScript<>(script, Long.class), Collections.singletonList(shortLinkCode), accountNo, 100);


        //加锁成功
        if (result > 0) {

            //C端处理
            if (EventMessageType.SHORT_LINK_ADD_LINK.name().equalsIgnoreCase(messageType)) {


                //先判断是否短链码被占用
                ShortLinkDO shortLinCodeDOInDB = shortLinkManager.findByShortLinkCode(shortLinkCode);

                if (shortLinCodeDOInDB == null) {
                    ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                            .accountNo(accountNo).code(shortLinkCode)
                            .title(addRequest.getTitle()).originalUrl(addRequest.getOriginalUrl())
                            .domain(domainDO.getValue()).groupId(linkGroupDO.getId())
                            .expired(addRequest.getExpired()).sign(originalUrlDigest)
                            .state(ShortLinkStateEnum.ACTIVE.name()).del(0).build();
                    shortLinkManager.addShortLink(shortLinkDO);
                    return true;
                } else {
                    log.error("C端短链码重复:{}", eventMessage);
                    duplicateCodeFlag = true;
                }


            } else if (EventMessageType.SHORT_LINK_ADD_MAPPING.name().equalsIgnoreCase(messageType)) {
                //B端处理
                GroupCodeMappingDO groupCodeMappingDOInDB = groupCodeMappingManager.findByCodeAndGroupId(shortLinkCode, linkGroupDO.getId(), accountNo);

                if (groupCodeMappingDOInDB == null) {

                    GroupCodeMappingDO groupCodeMappingDO = GroupCodeMappingDO.builder()
                            .accountNo(accountNo).code(shortLinkCode).title(addRequest.getTitle())
                            .originalUrl(addRequest.getOriginalUrl())
                            .domain(domainDO.getValue()).groupId(linkGroupDO.getId())
                            .expired(addRequest.getExpired()).sign(originalUrlDigest)
                            .state(ShortLinkStateEnum.ACTIVE.name()).del(0).build();

                    groupCodeMappingManager.add(groupCodeMappingDO);
                    return true;

                } else {
                    log.error("B端短链码重复:{}", eventMessage);
                    duplicateCodeFlag = true;
                }

            }

        } else {

            log.error("加锁失败:{}", eventMessage);

            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
            }

            duplicateCodeFlag = true;

        }

        if (duplicateCodeFlag) {
            String newOriginalUrl = CommonUtil.addUrlPrefixVersion(addRequest.getOriginalUrl());
            addRequest.setOriginalUrl(newOriginalUrl);
            eventMessage.setContent(JsonUtil.obj2Json(addRequest));
            log.warn("短链码报错失败，重新生成:{}", eventMessage);
            handleAddShortLink(eventMessage);
        }
        return false;
    }


    @Override
    public Map<String, Object> pageByGroupId(ShortLinkPageRequest request) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        return groupCodeMappingManager.pageShortLinkByGroupId(request.getPage(), request.getSize(), accountNo, request.getGroupId());
    }

    @Override
    public JsonData del(ShortLinkDelRequest request) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        EventMessage eventMessage = EventMessage.builder().accountNo(accountNo)
                .content(JsonUtil.obj2Json(request))
                .messageId(IDUtil.geneSnowFlakeID().toString())
                .eventMessageType(EventMessageType.SHORT_LINK_ADD_LINK.name()).build();
        rabbitTemplate.convertAndSend(
                rabbitMQConfig.getShortLinkEventExchange(),
                rabbitMQConfig.getShortLinkDelRoutingKey(), eventMessage
        );
        return JsonData.buildSuccess();

    }

    @Override
    public JsonData update(ShortLinkUpdateRequest request) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        EventMessage eventMessage = EventMessage.builder().accountNo(accountNo)
                .content(JsonUtil.obj2Json(request))
                .messageId(IDUtil.geneSnowFlakeID().toString())
                .eventMessageType(EventMessageType.SHORT_LINK_UPDATE_MAPPING.name())
                .build();
        rabbitTemplate.convertAndSend(rabbitMQConfig.getShortLinkEventExchange(), rabbitMQConfig.getShortLinkUpdateRoutingKey(), eventMessage);


        return JsonData.buildSuccess();
    }

    @Override
    public boolean handleDelShortLink(EventMessage eventMessage) {
        Long accountNo = eventMessage.getAccountNo();
        String messageType = eventMessage.getEventMessageType();
        ShortLinkDelRequest request = JsonUtil.json2Obj(eventMessage.getContent(), ShortLinkDelRequest.class);
        if (EventMessageType.SHORT_LINK_DEL_LINK.name().equalsIgnoreCase(messageType)) {
            ShortLinkDO shortLinkDO = ShortLinkDO.builder().code(request.getCode()).accountNo(accountNo).build();

            int rows = shortLinkManager.del(shortLinkDO);
            log.debug("删除C端短链:{}", rows);
            return true;
        } else if (EventMessageType.SHORT_LINK_ADD_MAPPING.name().equalsIgnoreCase(messageType)) {
            GroupCodeMappingDO groupCodeMappingDO = GroupCodeMappingDO.builder()
                    .id(request.getMappingId()).accountNo(accountNo)
                    .groupId(request.getGroupId()).build();
            int rows = groupCodeMappingManager.del(groupCodeMappingDO);
            log.debug("删除B端短链:{}", rows);
            return true;
        }
        return false;
    }

    @Override
    public boolean handleUpdateShortLink(EventMessage eventMessage) {

        Long accountNo = eventMessage.getAccountNo();
        String messageType = eventMessage.getEventMessageType();

        ShortLinkUpdateRequest request = JsonUtil.json2Obj(eventMessage.getContent(), ShortLinkUpdateRequest.class);

        //校验短链域名
        DomainDO domainDO = checkDomain(request.getDomainType(), request.getDomainId(), accountNo);

        //C端处理
        if (EventMessageType.SHORT_LINK_UPDATE_LINK.name().equalsIgnoreCase(messageType)) {

            ShortLinkDO shortLinkDO = ShortLinkDO.builder().code(request.getCode()).title(request.getTitle())
                    .domain(domainDO.getValue())
                    .accountNo(accountNo).build();

            int rows = shortLinkManager.update(shortLinkDO);
            log.debug("更新C端短链，rows={}", rows);
            return true;

        } else if (EventMessageType.SHORT_LINK_UPDATE_MAPPING.name().equalsIgnoreCase(messageType)) {
            //B端处理
            GroupCodeMappingDO groupCodeMappingDO = GroupCodeMappingDO.builder().id(request.getMappingId()).groupId(request.getGroupId())
                    .accountNo(accountNo)
                    .title(request.getTitle())
                    .domain(domainDO.getValue())
                    .build();

            int rows = groupCodeMappingManager.update(groupCodeMappingDO);
            log.debug("更新B端短链，rows={}", rows);
            return true;
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

