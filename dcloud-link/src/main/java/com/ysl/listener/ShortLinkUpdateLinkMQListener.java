package com.ysl.listener;


import com.ysl.enums.BizCodeEnum;
import com.ysl.enums.EventMessageType;
import com.ysl.exception.BizException;
import com.ysl.model.EventMessage;
import com.ysl.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.Channel;

@Component
@Slf4j
@RequiredArgsConstructor
@RabbitListener(queuesToDeclare = {@Queue("short_link.update.link.queue")})
public class ShortLinkUpdateLinkMQListener {
    private final ShortLinkService shortLinkService;


    @RabbitHandler
    public void shortLinkHandler(EventMessage eventMessage, Message message, Channel channel) throws IOException {
        log.info("监听到消息ShortLinkUpdateLinkMQListener message消息内容:{}", message);
        try {
            eventMessage.setEventMessageType(EventMessageType.SHORT_LINK_UPDATE_LINK.name());
            shortLinkService.handleUpdateShortLink(eventMessage);

        } catch (Exception e) {
            log.error("消费失败:{}", eventMessage);
            throw new BizException(BizCodeEnum.MQ_CONSUME_EXCEPTION);
        }
        log.info("消费成功:{}", eventMessage);
    }
}
