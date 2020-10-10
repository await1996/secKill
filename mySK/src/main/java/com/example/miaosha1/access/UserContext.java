package com.example.miaosha1.access;

import com.example.miaosha1.domain.MiaoshaUser;

public class UserContext {
    //ThreadLocal和当前线程绑定，其内的资源只属于该线程,此处将user与线程绑定
    private static ThreadLocal<MiaoshaUser> userThreadLocal=new ThreadLocal<>();

    public static void setUser(MiaoshaUser user){
        userThreadLocal.set(user);
    }

    public static MiaoshaUser getUser(){
        return userThreadLocal.get();
    }
}
