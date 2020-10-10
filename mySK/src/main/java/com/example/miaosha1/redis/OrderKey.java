package com.example.miaosha1.redis;

public class OrderKey extends BasePrefix{
    public OrderKey(String prefix) {
        super(prefix);
    }

    public OrderKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static OrderKey getMiaoshaOrderByUidGid = new OrderKey("mug");
}
