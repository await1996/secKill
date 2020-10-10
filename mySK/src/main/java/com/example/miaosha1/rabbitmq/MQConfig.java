package com.example.miaosha1.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {

    public static final String MIAOSHA_QUEUE="miaosha.queue";

    @Bean
    public Queue miaoshaQueue(){
        return new Queue(MIAOSHA_QUEUE,true);
    }

    /*public static final String QUEUE="queue";
    public static final String TOPIC_QUEUE1="topic.queue1";
    public static final String TOPIC_QUEUE2="topic.queue2";

    public static final String TOPIC_EXCHANGE="topicExchange";

    public static final String ROUTING_KEY1="topic.key1";
    public static final String ROUTING_KEY2="topic.#";//#表示任意个字符,因此可以兼容上面那个key
    *//**
     * 默认
     * Direct 模式 交换机Exchange
     * 先把消息放到Exchange，Exchange再把消息放到队列
     *//*
    @Bean
    public Queue queue(){
        return new Queue(QUEUE,true);
    }

    *//**
     * Topic 模式 交换机Exchange
     *将queue，exchange，key三者绑定。因此可以给某个key发消息，只有对应queue中可以接受到
     *//*
    @Bean
    public Queue topicQueue1(){
        return new Queue(TOPIC_QUEUE1,true);
    }
    @Bean
    public Queue topicQueue2(){
        return new Queue(TOPIC_QUEUE2,true);
    }

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(TOPIC_EXCHANGE);
    }
    @Bean//将queue，exchange，key三者绑定
    public Binding topicBinding1(){
        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with(ROUTING_KEY1);
    }
    @Bean
    public Binding topicBinding2(){
        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with(ROUTING_KEY2);
    }*/



}
