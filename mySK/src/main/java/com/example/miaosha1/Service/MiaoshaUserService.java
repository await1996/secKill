package com.example.miaosha1.Service;

import com.example.miaosha1.dao.MiaoshaUserDao;
import com.example.miaosha1.domain.MiaoshaUser;
import com.example.miaosha1.exception.GlobalException;
import com.example.miaosha1.redis.MiaoshaUserKey;
import com.example.miaosha1.result.CodeMsg;
import com.example.miaosha1.util.MD5Util;
import com.example.miaosha1.util.UUIDUtil;
import com.example.miaosha1.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class MiaoshaUserService {

    @Autowired
    MiaoshaUserDao miaoshaUserDao;

    @Autowired
    RedisService redisService;

    public static final String COOKIE_NAME_TOKEN = "token";

    public String login(HttpServletResponse response,LoginVo loginVo) {
        if(loginVo==null)
            throw new GlobalException(CodeMsg.SERVER_ERROR);
            //return CodeMsg.SERVER_ERROR;
        // 出现异常，不应该返回CodeMsg，而是直接抛出该异常,由异常处理器进行处理（返回CodeMsg）

        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();

        //判断手机号是否存在，密码是否正确
        MiaoshaUser user = getById(Long.parseLong(mobile));
        if(user==null)
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);

        String saltDB = user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
        String dbPass = user.getPassword();
        if(!calcPass.equals(dbPass))
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);

        //生成cookie
        String token = UUIDUtil.uuid();
        addCookie(response,token,user);
        return token;
    }

    //对象缓存
    public MiaoshaUser getById(long id){
        //取缓存
        //构造realKey：前者表明所属类，后者表明在该类中的id
        MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, "" + id, MiaoshaUser.class);
        if(user!=null)
            return user;

        //缓存中没有就取数据库
        user = miaoshaUserDao.getById(id);

        //查完数据库以后往缓存中放，并返回查询结果
        if(user!=null)
            redisService.set(MiaoshaUserKey.getById,""+id,user);
        return user;
    }

    //数据更新时（比如改密码）
    public boolean updatePassword(String token,long id,String passwordNew){
        //
        MiaoshaUser user = getById(id);
        if(user==null)
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);

        //更新数据库，同时要更新对应对象缓存
        //数据库不直接使用修改后的user放进去更新，而是new一个对象，赋值修改的字段，然后用新对象放入数据库更新
        MiaoshaUser newUser = new MiaoshaUser();
        newUser.setId(id);
        newUser.setPassword(MD5Util.formPassToDBPass(passwordNew,user.getSalt()));
        miaoshaUserDao.update(newUser);
        //更新缓存:删除+更新
        //对于通过id获取的user直接删除即可
        redisService.delete(MiaoshaUserKey.getById,""+id);
        //对于通过通过token获取的user必须更新（即delete+set操作），性能会降低，但是这样才能保证页面跳转不出错
        user.setPassword(newUser.getPassword());
        redisService.set(MiaoshaUserKey.token,token,user);

        return true;
    }

    public MiaoshaUser getByToken(HttpServletResponse response,String token) {
        if(StringUtils.isEmpty(token))
            return null;

        MiaoshaUser user = redisService.get(MiaoshaUserKey.token,token,MiaoshaUser.class);
        //在通过redis获取user的时候，再生成一次cookie，达到延长过期时间的效果
        if(user!=null){
            addCookie(response,token,user);
        }

        return user;
    }

    private void addCookie(HttpServletResponse response,String token,MiaoshaUser user){
        //通过UUID生成token，并标识token对应的用户（将映射信息存到第三方，此处使用缓存）
        //通过token生成cookie,并将cookie返回给客户端（通过httpResponse）
        //客户端下次访问时，会带上cookie，通过cookie中的token就知道是哪个用户了
        redisService.set(MiaoshaUserKey.token,token,user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN,token);
        cookie.setMaxAge(MiaoshaUserKey.token.getExpireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
