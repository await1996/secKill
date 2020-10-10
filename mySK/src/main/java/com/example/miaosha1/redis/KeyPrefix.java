package com.example.miaosha1.redis;

public interface KeyPrefix {

    public int getExpireSeconds();

    public String getPrefix();
}
