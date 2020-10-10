package com.example.miaosha1.Service;

import com.example.miaosha1.dao.GoodsDao;
import com.example.miaosha1.dao.OrderDao;
import com.example.miaosha1.domain.MiaoshaOrder;
import com.example.miaosha1.domain.MiaoshaUser;
import com.example.miaosha1.domain.OrderInfo;
import com.example.miaosha1.redis.OrderKey;
import com.example.miaosha1.vo.GoodsVo;
import com.sun.org.apache.xpath.internal.operations.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    OrderDao orderDao;

    @Autowired
    RedisService redisService;

    public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(Long userId, long goodsId) {
        //return orderDao.getMiaoshaOrderByUserIdGoodsId(userId,goodsId);查数据库改成查缓存
        return redisService.get(OrderKey.getMiaoshaOrderByUidGid,""+userId+"_"+goodsId,MiaoshaOrder.class);
    }

    @Transactional//同时要写缓存
    public OrderInfo createOrder(MiaoshaUser user, GoodsVo goods) {
        //创建订单
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getMiaoshaPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
        orderDao.insert(orderInfo);

        //创建秒杀订单
        MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
        miaoshaOrder.setOrderId(orderInfo.getId());
        miaoshaOrder.setGoodsId(goods.getId());
        miaoshaOrder.setUserId(user.getId());
        orderDao.insertMiaoshaOrder(miaoshaOrder);

        //写缓存
        redisService.set(OrderKey.getMiaoshaOrderByUidGid,""+user.getId()+"_"+goods.getId(),miaoshaOrder);

        return orderInfo;
    }

    public OrderInfo getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);
    }

    public void deleteOrders() {
        orderDao.deleteOrders();
        orderDao.deleteMiaoshaOrders();
    }
}
