package com.example.miaosha1.controller;

import com.example.miaosha1.Service.*;
import com.example.miaosha1.access.AccessLimit;
import com.example.miaosha1.domain.MiaoshaOrder;
import com.example.miaosha1.domain.MiaoshaUser;
import com.example.miaosha1.domain.OrderInfo;
import com.example.miaosha1.rabbitmq.MQSender;
import com.example.miaosha1.rabbitmq.MiaoshaMessage;
import com.example.miaosha1.redis.AccessKey;
import com.example.miaosha1.redis.GoodsKey;
import com.example.miaosha1.redis.MiaoshaKey;
import com.example.miaosha1.redis.OrderKey;
import com.example.miaosha1.result.CodeMsg;
import com.example.miaosha1.result.Result;
import com.example.miaosha1.vo.GoodsVo;
import com.sun.tracing.dtrace.ArgsAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {
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

    @Autowired
    MQSender sender;

    private static Logger log = LoggerFactory.getLogger(MiaoshaController.class);

    private Map<Long,Boolean> localOverMap = new HashMap<>();

    //系统初始化,加载商品数量      秒杀接口优化第一步
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if(goodsList==null)
            return;

        for(GoodsVo goods:goodsList){
            redisService.set(GoodsKey.getMiaoshaGoodsStock,""+goods.getId(),goods.getStockCount());
            localOverMap.put(goods.getId(),false);
        }
    }

    /*get post区别
    *
    * Get幂等，调用以后服务端数据不变
    * Put非幂等，调用以后服务端数据发生变化
    * */
    @RequestMapping(value = "/{path}do_miaosha")//post请求
    @ResponseBody//修改为通过ajax发出请求
    public Result<Integer> miaosha(Model model, MiaoshaUser user, @RequestParam("goodsId")long goodsId,
                                   @PathVariable("path")String path) {
        System.out.println("/miaosha/do_miaosha");

        model.addAttribute("user",user);

        if(user==null)
            return Result.error(CodeMsg.SESSION_ERROR);

        //验证path
        boolean check = miaoshaService.checkPath(user, goodsId, path);
        if(!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }


        //redis预减库存前，通过map先判断当前商品ID是否已经被买了
        boolean over=localOverMap.get(goodsId);
        if(over){
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        //判断库存2，redis预减库存 秒杀接口优化第二步
        Long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
        if(stock<0){
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }


        //判断重复秒杀  一次mysql访问改成redis访问
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if(order!=null){
            return Result.error(CodeMsg.MIAO_SHA_REPEATED);
        }

        //入队    秒杀接口优化第三步
        MiaoshaMessage message = new MiaoshaMessage();
        message.setUser(user);
        message.setGoodsId(goodsId);
        sender.sendMiaoshaMessage(message);

        return Result.success(0);//0表示排队中

        /*
        //判断库存 一次mysql访问
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        Integer stock = goods.getStockCount();
        if(stock <= 0){
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        //判断重复秒杀  一次mysql访问改成redis访问
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if(order!=null){
            return Result.error(CodeMsg.MIAO_SHA_REPEATED);
        }

        //三次mysql访问   减库存一次、生成订单两次（普通订单、秒杀订单）
        //执行秒杀（事务）  减库存、生成订单    并将订单放入model中，前端可以获取订单信息
        OrderInfo orderInfo = miaoshaService.miaosha(user,goods);

        //return "order_detail";
        System.out.println(stock);
        return Result.success(orderInfo);
        */
    }

    //给出秒杀结果
    //orderId 成功，-1 秒杀失败  ，0  排队中
    @GetMapping("/result")
    @ResponseBody
    public Result<Long> miaoshaResult(Model model, MiaoshaUser user, @RequestParam("goodsId")long goodsId) {
        System.out.println("/miaosha/result");
        model.addAttribute("user", user);

        if (user == null)
            return Result.error(CodeMsg.SESSION_ERROR);

        long res = miaoshaService.getMiaoshaResult(user.getId(),goodsId);

        return Result.success(res);
    }


    //还原redis、mysql初始数据
    @RequestMapping(value="/reset", method=RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model) {
        System.out.println("/miaosha/do_miaosha");

        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        for(GoodsVo goods : goodsList) {
            goods.setStockCount(10);
            redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), 10);
            localOverMap.put(goods.getId(), false);
        }
        redisService.delete(OrderKey.getMiaoshaOrderByUidGid);
        redisService.delete(MiaoshaKey.isGoodsOver);
        miaoshaService.reset(goodsList);
        return Result.success(true);
    }

    @AccessLimit(seconds=5,maxCount=5,needLogin=true)
    @RequestMapping(value="/path", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(HttpServletRequest request,MiaoshaUser user,
                                         @RequestParam("goodsId")long goodsId,
                                         @RequestParam(value="verifyCode",defaultValue = "0")int verifyCode)
    {                                    //加个defaultValue，可以不输入验证码值
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        //先验证验证码
        boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
        if(!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        String path = miaoshaService.createMiaoshaPath(user, goodsId);
        return Result.success(path);
    }

    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCod(HttpServletResponse response, MiaoshaUser user,
                                              @RequestParam("goodsId")long goodsId) {
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        try {
            BufferedImage image  = miaoshaService.createVerifyCode(user, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        }catch(Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }
}
