package com.example.miaosha1.controller;

import com.example.miaosha1.Service.GoodsService;
import com.example.miaosha1.Service.OrderService;
import com.example.miaosha1.domain.MiaoshaUser;
import com.example.miaosha1.domain.OrderInfo;
import com.example.miaosha1.result.CodeMsg;
import com.example.miaosha1.result.Result;
import com.example.miaosha1.vo.GoodsVo;
import com.example.miaosha1.vo.OrderDetailVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;

    @RequestMapping("/detail")
    @ResponseBody
    //@NeedLogin,很多地方都用到了用户登录，可以自定义一个拦截器，加了该注解就要判断是否登录
    public Result<OrderDetailVo> info(@Param("orderOd")long orderId, MiaoshaUser user){
        System.out.println("/order/detail");

        if(user==null)
            return Result.error(CodeMsg.SESSION_ERROR);

        //获取订单
        OrderInfo orderInfo = orderService.getOrderById(orderId);
        if(orderInfo==null)
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        //获取商品
        Long goodsId = orderInfo.getGoodsId();
        GoodsVo goodVo = goodsService.getGoodsVoByGoodsId(goodsId);

        //构造订单详情
        OrderDetailVo orderDetailVo=new OrderDetailVo();
        orderDetailVo.setOrderInfo(orderInfo);
        orderDetailVo.setGoodsVo(goodVo);

        return Result.success(orderDetailVo);
    }

}
