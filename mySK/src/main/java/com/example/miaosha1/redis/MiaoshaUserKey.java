package com.example.miaosha1.redis;

public class MiaoshaUserKey extends BasePrefix{
    public static final int TOKEN_EXPIRE = 2*24*3600;

    public MiaoshaUserKey(int expireSeconds,String prefix) {
        super(expireSeconds,prefix);
    }

    //这里调用了父类的构造方法，参数为tk，最后构造了一个expireSeconds为0，prefix为tk的KeyPrefix对象
    public static MiaoshaUserKey token = new MiaoshaUserKey(TOKEN_EXPIRE,"tk");

    //用户对象变化很小，设置永久有效
    public static MiaoshaUserKey getById = new MiaoshaUserKey(0,"id");
}
