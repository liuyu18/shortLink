package com.ysl.listener;


import com.rabbitmq.client.Channel;
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

@Component
@Slf4j
@RabbitListener(queuesToDeclare = {@Queue("short_link.add.link.queue")})
@RequiredArgsConstructor
public class ShortLinkAddLinkMQListener {


    private final ShortLinkService shortLinkService;

    @RabbitHandler
    public void shortLinkHandler(
            EventMessage eventMessage,
            Message message, Channel channel) throws IOException {
        log.info("[short-link-add][listener-link] receive messageId={}, rawMessage={}", eventMessage.getMessageId(), message);
        try {

            //TODO 处理业务逻辑
            eventMessage.setEventMessageType(EventMessageType.SHORT_LINK_ADD_LINK.name());
            boolean result = shortLinkService.handleAddShortLink(eventMessage);
            log.info("[short-link-add][listener-link] handle result messageId={}, result={}, content={}",
                    eventMessage.getMessageId(), result, eventMessage.getContent());

        } catch (IllegalArgumentException e) {

            log.warn("[short-link-add][listener-link] invalid business message, discard without requeue messageId={}, reason={}, eventMessage={}",
                    eventMessage.getMessageId(), e.getMessage(), eventMessage);
            return;
        } catch (Exception e) {

            //处理业务异常，还有进行其他操作，比如记录失败原因
            log.error("[short-link-add][listener-link] consume failed messageId={}, eventMessage={}", eventMessage.getMessageId(), eventMessage, e);
            throw new BizException(BizCodeEnum.MQ_CONSUME_EXCEPTION);
        }
        log.info("[short-link-add][listener-link] consume success messageId={}, eventMessage={}", eventMessage.getMessageId(), eventMessage);
        //确认消息消费成功
        //channel.basicAck(tag,false);

    }


}
