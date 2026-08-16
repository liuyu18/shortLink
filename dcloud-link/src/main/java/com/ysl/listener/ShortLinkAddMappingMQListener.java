package com.ysl.listener;

import com.ysl.enums.BizCodeEnum;
import com.ysl.enums.EventMessageType;
import com.ysl.exception.BizException;
import com.ysl.model.EventMessage;
import com.ysl.service.ShortLinkService;
import groovy.util.logging.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.Channel;

@lombok.extern.slf4j.Slf4j
@Component
@Slf4j
@RabbitListener(queuesToDeclare = {@Queue("short_link.add.mapping.queue")})
public class ShortLinkAddMappingMQListener {

    private final ShortLinkService shortLinkService;

    public ShortLinkAddMappingMQListener(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    @RabbitHandler
    public void shortLinkHandler(
            EventMessage eventMessage,
            Message message,
            Channel channel

    ) throws IOException {
        log.info("[short-link-add][listener-mapping] receive messageId={}, rawMessage={}", eventMessage.getMessageId(), message);

        try {

            eventMessage.setEventMessageType(EventMessageType.SHORT_LINK_ADD_MAPPING.name());
            boolean result = shortLinkService.handleAddShortLink(eventMessage);
            log.info("[short-link-add][listener-mapping] handle result messageId={}, result={}, content={}",
                    eventMessage.getMessageId(), result, eventMessage.getContent());

        } catch (IllegalArgumentException e) {
            log.warn("[short-link-add][listener-mapping] invalid business message, discard without requeue messageId={}, reason={}, eventMessage={}",
                    eventMessage.getMessageId(), e.getMessage(), eventMessage);
            return;
        } catch (Exception e) {
            log.error("[short-link-add][listener-mapping] consume failed messageId={}, eventMessage={}", eventMessage.getMessageId(), eventMessage, e);
            throw new BizException(BizCodeEnum.MQ_CONSUME_EXCEPTION);
        }
        log.info("[short-link-add][listener-mapping] consume success messageId={}, eventMessage={}", eventMessage.getMessageId(), eventMessage);

    }
}
