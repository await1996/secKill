package com.example.miaosha1.rabbitmq;

import com.example.miaosha1.Service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQSender {

    @Autowired
    AmqpTemplate amqpTemplate;

    private static Logger log= LoggerFactory.getLogger(MQReceiver.class);

    public void sendMiaoshaMessage(MiaoshaMessage message) {
        String msg = RedisService.beanToString(message);
        log.info("send msg:"+msg);
        amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE,msg);
    }

    /*public void send(Object message){
        String msg= RedisService.beanToString(message);
        log.info("send msg:"+msg);
        amqpTemplate.convertAndSend(MQConfig.QUEUE,msg);
    }

    public void sendTopic(Object message){
        String msg= RedisService.beanToString(message);
        log.info("send topic msg:"+msg);
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,MQConfig.ROUTING_KEY1,msg+"1");
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,MQConfig.ROUTING_KEY2,msg+"2");
    }*/

}
