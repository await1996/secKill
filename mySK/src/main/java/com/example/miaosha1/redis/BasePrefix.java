package com.example.miaosha1.redis;

public abstract class BasePrefix implements KeyPrefix {//定义为抽象类
    private int expireSeconds;
    private String prefix;

    public BasePrefix(String prefix) {//0代表不过期
        this.expireSeconds = 0;
        this.prefix = prefix;
    }

    public BasePrefix(int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }

    @Override
    public int getExpireSeconds() {
        return expireSeconds;
    }

    @Override
    public String getPrefix() {
        String className = getClass().getSimpleName();
        return className+":"+prefix;
    }
}
