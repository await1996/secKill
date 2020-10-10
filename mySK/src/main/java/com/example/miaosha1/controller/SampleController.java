package com.example.miaosha1.controller;

import com.example.miaosha1.Service.RedisService;
import com.example.miaosha1.domain.User;
import com.example.miaosha1.rabbitmq.MQSender;
import com.example.miaosha1.redis.UserKey;
import com.example.miaosha1.result.CodeMsg;
import com.example.miaosha1.Service.UserService;
import com.example.miaosha1.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class SampleController {

    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender sender;

    /*@RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> topic(){
        sender.sendTopic("hello,topic");
        return Result.success("hello,cqupt:topic");
        //return new Result(0,"success","hello,cqupt");
    }

    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq(){
        sender.send("hello,mq");
        return Result.success("hello,cqupt:mq");
        //return new Result(0,"success","hello,cqupt");
    }*/

    @RequestMapping("/hello")
    @ResponseBody
    public Result<String> hello(){
        return Result.success("hello,cqupt");
        //return new Result(0,"success","hello,cqupt");
    }

    @RequestMapping("/helloError")
    @ResponseBody
    public Result<String> helloError(){
        return Result.error(CodeMsg.SERVER_ERROR);
        //return new Result(500100,"服务端异常");
    }

    @RequestMapping("/thymeleaf")
    public String Thymeleaf(Model model){
        model.addAttribute("msg","thymeleaf");
        return "thymeleaf";
    }

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> dbGet(){
        User user = userService.getById(1);
        return Result.success(user);
    }

    @RequestMapping("/db/tx")
    @ResponseBody
    public Result<Boolean> dbTx(){
        userService.tx();

        return Result.success(true);
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet(){//通过
        //UserKey.getById，这里调用了BasePrefix的构造方法，参数为id，最后生成了一个expireSeconds为0，prefix为id的KeyPrefix对象
        //通过该对象可以获取对象的expireSeconds和构造后的prefix
        User user = redisService.get(UserKey.getById,"1",User.class);//构造后的key为UserKey:id1
        return Result.success(user);     //UserKey为类名  id为prefix  1为原始key
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet(){
        User user = new User();
        user.setId(1);
        user.setName("111");
        redisService.set(UserKey.getById,"1",user);

        return Result.success(true);
    }

}
