package com.ysl.config;

import lombok.Data;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class RabbitMQConfig {
    // 交换机
    private String shortLinkEventExchange = "short_link.event.exchange";


    @Bean
    public Exchange shortLinkEventExchange() {
        return new TopicExchange(shortLinkEventExchange, true,false);
    }

    private String shortLinkAddLinkQueue="short_link.add.link.queue";

    private String shortLinkAddMappingQueue="short_link.add.mapping.queue";
    private String shortLinkAddRoutingKey="short_link.add.link.mapping.routing.key";
    private String shortLinkAddLinkBindingKey="short_link.add.link.*.routing.key";
    private String shortLinkAddMappingBindingKey="short_link.add.*.mapping.routing.key";

    @Bean
    public Binding shortLinkAddApiBinding(){
        return new Binding(
                shortLinkAddLinkQueue,
                Binding.DestinationType.QUEUE,
                shortLinkEventExchange,
                shortLinkAddLinkBindingKey,
                null);
    }

    @Bean
    public Queue shortLinkAddApiQueue(){
        return new Queue(shortLinkAddLinkQueue, true, false,false);

    }

    @Bean
    public Queue shortLinkAddMappingQueue(){

        return new Queue(
                shortLinkAddMappingQueue,
                true,
                false,
                false);

    }

}
