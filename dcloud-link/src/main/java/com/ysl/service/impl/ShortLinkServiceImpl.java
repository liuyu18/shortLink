package com.ysl.service.impl;

import com.ysl.config.RabbitMQConfig;
import com.ysl.controller.request.ShortLinkAddRequest;
import com.ysl.enums.EventMessageType;
import com.ysl.interceptor.LoginInterceptor;
import com.ysl.manager.ShortLinkManager;
import com.ysl.model.EventMessage;
import com.ysl.model.ShortLinkDO;
import com.ysl.service.ShortLinkService;
import com.ysl.util.IDUtil;
import com.ysl.util.JsonData;
import com.ysl.util.JsonUtil;
import com.ysl.vo.ShortLinkVO;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
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
}
