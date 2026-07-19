package com.ysl.listener;

import com.ysl.enums.BizCodeEnum;
import com.ysl.exception.BizException;
import com.ysl.model.EventMessage;
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

    @RabbitHandler
    public void shortLinkHandler(
            EventMessage eventMessage,
            Message message,
            Channel channel

    ) throws IOException {
        log.info("监听到消息ShortLinkAddLinkMQListener message消息内容:{}", message);

        try {

        } catch (Exception e) {
            log.error("消费失败:{}", eventMessage);
            throw new BizException(BizCodeEnum.MQ_CONSUME_EXCEPTION);
        }
        log.info("消费成功:{}", eventMessage);

    }
}
