package com.example.miaosha1.controller;

import com.example.miaosha1.Service.MiaoshaUserService;
import com.example.miaosha1.Service.UserService;
import com.example.miaosha1.result.CodeMsg;
import com.example.miaosha1.result.Result;
import com.example.miaosha1.util.ValidatorUtil;
import com.example.miaosha1.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {
    @Autowired
    MiaoshaUserService miaoshaUserService;

    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping("/to_login")
    public String toLogin() {
        System.out.println("to_login");
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response,@Valid LoginVo loginVo) {
        System.out.println("do_login");
        log.info(loginVo.toString());
        /*//参数校验
        String passInput = loginVo.getPassword();
        String mobile = loginVo.getMobile();
        //在controller里先检查手机号、密码是否有格式错误
        if(StringUtils.isEmpty(passInput)){
            return Result.error(CodeMsg.PASSWORD_EMPTY);
        }
        if(StringUtils.isEmpty(mobile)){
            return Result.error(CodeMsg.MOBILE_EMPTY);
        }
        if(!ValidatorUtil.isMobile(mobile)){
            System.out.println("手机号格式错误");
            return Result.error(CodeMsg.MOBILE_ERROR);
        }*/

        //登录
        //在service里继续检查手机号是否存在、密码是否正确
        //成功就返回token，失败由异常处理器进行处理
        String token = miaoshaUserService.login(response, loginVo);
        return Result.success(token);
    }
}
