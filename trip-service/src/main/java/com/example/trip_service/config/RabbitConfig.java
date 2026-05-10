package com.example.trip_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String TRIP_EXCHANGE = "trip.exchange";
    public static final String USER_EXCHANGE = "user.exchange";
    public static final String TRIP_QUEUE = "trip.queue";

    @Bean
    public TopicExchange tripExchange() {
        return new TopicExchange(TRIP_EXCHANGE);
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    public Queue tripQueue() {
        return new Queue(TRIP_QUEUE, true);
    }

    @Bean
    public Binding tripBinding(Queue tripQueue, TopicExchange tripExchange) {
        return BindingBuilder
                .bind(tripQueue)
                .to(tripExchange)
                .with("trip.*");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}