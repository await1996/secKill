package com.example.miaosha1.controller;

import com.example.miaosha1.domain.MiaoshaUser;
import com.example.miaosha1.result.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
public class UserController {

    @RequestMapping("/info")
    @ResponseBody
    public Result<MiaoshaUser> info(MiaoshaUser miaoshaUser){
        System.out.println("/user/info");
        return Result.success(miaoshaUser);
    }
}
