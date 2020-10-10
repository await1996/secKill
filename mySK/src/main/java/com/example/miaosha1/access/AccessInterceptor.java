package com.example.miaosha1.access;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.example.miaosha1.Service.MiaoshaUserService;
import com.example.miaosha1.Service.RedisService;
import com.example.miaosha1.domain.MiaoshaUser;
import com.example.miaosha1.redis.AccessKey;
import com.example.miaosha1.result.CodeMsg;
import com.example.miaosha1.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    MiaoshaUserService userService;

    @Autowired
    RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){
            //拿到user,并将user放到threadLocal中(从收到请求到请求结束，使用同一个线程执行)
            MiaoshaUser user = getUser(request, response);
            UserContext.setUser(user);

            //拿到方法上的注解
            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if(accessLimit==null){
                return true;
            }

            //限流逻辑代码
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();

            String key = request.getRequestURI();
            if(needLogin){
                if(user==null){
                    render(response, CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_"+user.getId();
            }else{

            }

            //查询访问次数(使用redis),设定5秒只能访问5次
            AccessKey ak = AccessKey.withExpire(seconds);
            Integer count = redisService.get(ak, "" + key, Integer.class);
            if(count==null){
                redisService.set(ak,key,1);
            }else if(count<5){
                redisService.incr(ak,key);
            }else{
                render(response,CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }

        }
        return true;
    }

    private void render(HttpServletResponse response,CodeMsg cm)throws Exception{
        response.setContentType("application/json;charset=UTF-8");//不然可能乱码
        ServletOutputStream out = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();

    }

    public MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response){
        String paramToken = request.getParameter(MiaoshaUserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request,MiaoshaUserService.COOKIE_NAME_TOKEN);

        if(StringUtils.isEmpty(paramToken) && StringUtils.isEmpty(cookieToken))
            return null;

        String token = StringUtils.isEmpty(paramToken) ? cookieToken:paramToken;
        MiaoshaUser user = userService.getByToken(response,token);

        return user;
    }

    private String getCookieValue(HttpServletRequest request, String cookieNameToken) {
        Cookie[] cookies = request.getCookies();

        if(cookies==null||cookies.length==0)
            return null;
        for(Cookie cookie:cookies){
            if(cookie.getName().equals(cookieNameToken))
                return cookie.getValue();
        }

        return null;
    }
}
