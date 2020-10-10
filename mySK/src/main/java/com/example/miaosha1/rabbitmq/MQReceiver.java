package com.example.miaosha1.rabbitmq;

import com.example.miaosha1.Service.*;
import com.example.miaosha1.domain.MiaoshaOrder;
import com.example.miaosha1.domain.MiaoshaUser;
import com.example.miaosha1.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {
    @Autowired
    MiaoshaUserService miaoshaUserService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    RedisService redisService;

    private static Logger log= LoggerFactory.getLogger(MQReceiver.class);

    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void receive(String msg){
        log.info("receive msg:"+msg);
        MiaoshaMessage message = RedisService.stringToBean(msg, MiaoshaMessage.class);
        MiaoshaUser user=message.getUser();
        long goodsId = message.getGoodsId();

        //到这里才重复之前controller做的5次mysql访问
        //判断库存 真正下单时才做mysql访问
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        Integer stock = goods.getStockCount();
        if(stock <= 0){
            return;
        }

        //判断重复秒杀  一次mysql访问改成redis访问（其实也可以不访问，因为mysql已经对这两个字段加了唯一索引）
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if(order!=null){
            return;
        }

        //三次mysql访问   减库存一次、生成订单两次（普通订单、秒杀订单）
        //执行秒杀（事务）  减库存、生成订单    并将订单放入model中，前端可以获取订单信息
        miaoshaService.miaosha(user,goods);
    }

    /*@RabbitListener(queues = MQConfig.QUEUE)
    public void receive(String message){
        log.info("receive msg:"+message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String message){
        log.info("topic queue1 msg:"+message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String message){
        log.info("topic queue2 msg:"+message);
    }*/
}
