package com.ysl.listener;


import com.rabbitmq.client.Channel;
import com.ysl.enums.BizCodeEnum;
import com.ysl.enums.EventMessageType;
import com.ysl.exception.BizException;
import com.ysl.model.EventMessage;
import com.ysl.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
@RabbitListener(queuesToDeclare = {@Queue("short_link.update.mapping.queue")})
public class ShortLinkUpdateMappingMQListener {


    private final ShortLinkService shortLinkService;

    @RabbitHandler
    public void shortLinkHandler(EventMessage eventMessage, Message message, Channel channel) throws IOException {
        log.info("监听到消息ShortLinkUpdateMappingMQListener message消息内容:{}", message);
        try {

            eventMessage.setEventMessageType(EventMessageType.SHORT_LINK_UPDATE_MAPPING.name());

            shortLinkService.handleUpdateShortLink(eventMessage);

        } catch (Exception e) {

            //处理业务异常，还有进行其他操作，比如记录失败原因
            log.error("消费失败:{}", eventMessage);
            throw new BizException(BizCodeEnum.MQ_CONSUME_EXCEPTION);
        }
        log.info("消费成功:{}", eventMessage);

    }


}
