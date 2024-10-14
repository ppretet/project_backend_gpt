package com.example.gpt.config;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MqConfig {
    @Bean
    public Queue delay_queue_order(){
        return new Queue("delay_queue_order");
    }
    @Bean
    public CustomExchange delay_exchange(){
        Map<String,Object> args = new HashMap<>();
        args.put("x-delayed-type","direct");
        return new CustomExchange("delay_exchange","x-delayed-message",true,false,args);
    }

    @Bean
    public Binding binddelay_queue_order(){
        return BindingBuilder.bind(delay_queue_order()).to(delay_exchange()).with("delay_queue_order").noargs();
    }
    @Bean
    public Queue use_queue(){
        return new Queue("use_queue");
    }
    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange("directExchange_use_queue");
    }

    @Bean
    public Binding bind_use_queue(){
        return BindingBuilder.bind(use_queue()).to(directExchange()).with("modularised");
    }


}
