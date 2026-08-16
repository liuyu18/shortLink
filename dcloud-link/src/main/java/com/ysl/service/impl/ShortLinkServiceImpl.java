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

    private static final int ADD_SHORT_LINK_MAX_RETRY = 5;

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
        log.info("[short-link-add][service] start accountNo={}, groupId={}, title={}, originalUrl={}, domainId={}, domainType={}, expired={}",
                accountNo, request.getGroupId(), request.getTitle(), request.getOriginalUrl(), request.getDomainId(), request.getDomainType(), request.getExpired());
        String newOriginalUrl = CommonUtil.addUrlPrefix(request.getOriginalUrl());
        request.setOriginalUrl(newOriginalUrl);

        EventMessage eventMessage = EventMessage.builder().accountNo(accountNo).
                content(JsonUtil.obj2Json(request))
                .messageId(IDUtil.geneSnowFlakeID().toString())
                .eventMessageType(EventMessageType.SHORT_LINK_ADD.name())
                .build();
        log.info("[short-link-add][mq-send] exchange={}, routingKey={}, messageId={}, accountNo={}, content={}",
                rabbitMQConfig.getShortLinkEventExchange(), rabbitMQConfig.getShortLinkAddRoutingKey(),
                eventMessage.getMessageId(), accountNo, eventMessage.getContent());
        rabbitTemplate.convertAndSend(
                rabbitMQConfig.getShortLinkEventExchange(),
                rabbitMQConfig.getShortLinkAddRoutingKey(),
                eventMessage
        );
        log.info("[short-link-add][mq-send] success messageId={}", eventMessage.getMessageId());
        return JsonData.buildSuccess();
    }

    @Override
    public boolean handleAddShortLink(EventMessage eventMessage) {
        return handleAddShortLink(eventMessage, 0);
    }

    private boolean handleAddShortLink(EventMessage eventMessage, int retryCount) {

        Long accountNo = eventMessage.getAccountNo();
        String messageType = eventMessage.getEventMessageType();
        log.info("[short-link-add][handler] start messageId={}, type={}, accountNo={}, retryCount={}, content={}",
                eventMessage.getMessageId(), messageType, accountNo, retryCount, eventMessage.getContent());

        ShortLinkAddRequest addRequest = JsonUtil.json2Obj(eventMessage.getContent(), ShortLinkAddRequest.class);
        log.info("[short-link-add][handler] parsed request messageId={}, groupId={}, title={}, originalUrl={}, domainId={}, domainType={}, expired={}",
                eventMessage.getMessageId(), addRequest.getGroupId(), addRequest.getTitle(), addRequest.getOriginalUrl(),
                addRequest.getDomainId(), addRequest.getDomainType(), addRequest.getExpired());
        //短链域名校验
        DomainDO domainDO = checkDomain(addRequest.getDomainType(), addRequest.getDomainId(), accountNo);
        //校验组是否合法
        LinkGroupDO linkGroupDO = checkLinkGroup(addRequest.getGroupId(), accountNo);
        log.info("[short-link-add][handler] check success messageId={}, domain={}, groupId={}, accountNo={}",
                eventMessage.getMessageId(), domainDO.getValue(), linkGroupDO.getId(), accountNo);

        //长链摘要
        String originalUrlDigest = CommonUtil.MD5(addRequest.getOriginalUrl());

        //短链码重复标记
        boolean duplicateCodeFlag = false;

        //生成短链码
        String shortLinkCode = shortLinkComponent.createShortLinkCode(addRequest.getOriginalUrl());
        log.info("[short-link-add][handler] generated code messageId={}, code={}, sign={}, originalUrl={}",
                eventMessage.getMessageId(), shortLinkCode, originalUrlDigest, addRequest.getOriginalUrl());

        //加锁
        //key1是短链码，ARGV[1]是accountNo,ARGV[2]是过期时间
        String script = "if redis.call('EXISTS',KEYS[1])==0 then redis.call('set',KEYS[1],ARGV[1]); redis.call('expire',KEYS[1],ARGV[2]); return 1;" +
                " elseif redis.call('get',KEYS[1]) == ARGV[1] then return 2;" +
                " else return 0; end;";

        Long result = redisTemplate.execute(new
                DefaultRedisScript<>(script, Long.class), Collections.singletonList(shortLinkCode), accountNo, 100);
        log.info("[short-link-add][redis-lock] messageId={}, code={}, accountNo={}, result={}",
                eventMessage.getMessageId(), shortLinkCode, accountNo, result);


        //加锁成功
        if (result != null && result > 0) {

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
                    int rows = shortLinkManager.addShortLink(shortLinkDO);
                    log.info("[short-link-add][db-write][link] success messageId={}, rows={}, code={}, groupId={}, accountNo={}, domain={}",
                            eventMessage.getMessageId(), rows, shortLinkCode, linkGroupDO.getId(), accountNo, domainDO.getValue());
                    return rows == 1;
                } else {
                    log.error("[short-link-add][duplicate][link] messageId={}, code={}, accountNo={}, dbId={}",
                            eventMessage.getMessageId(), shortLinkCode, accountNo, shortLinCodeDOInDB.getId());
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

                    int rows = groupCodeMappingManager.add(groupCodeMappingDO);
                    log.info("[short-link-add][db-write][mapping] success messageId={}, rows={}, code={}, groupId={}, accountNo={}, domain={}",
                            eventMessage.getMessageId(), rows, shortLinkCode, linkGroupDO.getId(), accountNo, domainDO.getValue());
                    return rows == 1;

                } else {
                    log.error("[short-link-add][duplicate][mapping] messageId={}, code={}, groupId={}, accountNo={}, mappingId={}",
                            eventMessage.getMessageId(), shortLinkCode, linkGroupDO.getId(), accountNo, groupCodeMappingDOInDB.getId());
                    duplicateCodeFlag = true;
                }

            }

        } else {

            log.error("[short-link-add][redis-lock] failed messageId={}, code={}, accountNo={}, result={}",
                    eventMessage.getMessageId(), shortLinkCode, accountNo, result);

            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
            }

            duplicateCodeFlag = true;

        }

        if (duplicateCodeFlag) {
            if (retryCount >= ADD_SHORT_LINK_MAX_RETRY) {
                log.error("[short-link-add][handler] retry exhausted messageId={}, type={}, accountNo={}, retryCount={}, content={}",
                        eventMessage.getMessageId(), messageType, accountNo, retryCount, eventMessage.getContent());
                return false;
            }
            String newOriginalUrl = CommonUtil.addUrlPrefixVersion(addRequest.getOriginalUrl());
            addRequest.setOriginalUrl(newOriginalUrl);
            eventMessage.setContent(JsonUtil.obj2Json(addRequest));
            log.warn("[short-link-add][handler] duplicate retry messageId={}, nextRetryCount={}, oldCode={}, newOriginalUrl={}",
                    eventMessage.getMessageId(), retryCount + 1, shortLinkCode, newOriginalUrl);
            return handleAddShortLink(eventMessage, retryCount + 1);
        }
        log.warn("[short-link-add][handler] unsupported message type or no-op messageId={}, type={}, content={}",
                eventMessage.getMessageId(), messageType, eventMessage.getContent());
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
        log.info("[short-link-add][check-domain] domainType={}, domainId={}, accountNo={}", domainType, domainId, accountNo);
        if (DomainTypeEnum.CUSTOM.name().equalsIgnoreCase(domainType)) {
            domainDO = domainManager.findById(domainId, accountNo);
        } else {
            domainDO = domainManager.findByDomainTypeAndID(domainId, DomainTypeEnum.OFFICIAL);
        }
        Assert.notNull(domainDO, "短链域名不合法");
        log.info("[short-link-add][check-domain] success domainId={}, accountNo={}, domain={}", domainId, accountNo, domainDO.getValue());
        return domainDO;

    }

    private LinkGroupDO checkLinkGroup(Long groupId, Long accountNo) {
        log.info("[short-link-add][check-group] groupId={}, accountNo={}", groupId, accountNo);
        LinkGroupDO linkGroupDO = linkGroupManager.detail(groupId, accountNo);
        if (linkGroupDO == null) {
            log.warn("[short-link-add][check-group] failed group not found or not owned by account groupId={}, accountNo={}", groupId, accountNo);
        }
        Assert.notNull(linkGroupDO, "组名不合法");
        log.info("[short-link-add][check-group] success groupId={}, accountNo={}, title={}", groupId, accountNo, linkGroupDO.getTitle());
        return linkGroupDO;
    }

}
