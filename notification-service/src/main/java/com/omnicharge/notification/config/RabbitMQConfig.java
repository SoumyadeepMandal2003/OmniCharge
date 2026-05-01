package com.omnicharge.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE      = "omnicharge.exchange";
    public static final String QUEUE         = "recharge.completed.queue";
    public static final String ROUTING_KEY   = "recharge.completed";

    @Bean
    public TopicExchange omnichargeExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue rechargeQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(rechargeQueue())
                .to(omnichargeExchange())
                .with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(messageConverter);
        return t;
    }
}
