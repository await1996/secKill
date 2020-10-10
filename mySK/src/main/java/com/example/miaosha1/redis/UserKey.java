package com.example.miaosha1.redis;

public class UserKey extends BasePrefix{


    private UserKey(String prefix) {
        super(prefix);
    }

    public static UserKey getById = new UserKey("id");//这里调用了父类的构造方法，参数为id，最后构造了一个expireSeconds为0，prefix为id的KeyPrefix对象
    public static UserKey getByName = new UserKey("name");
}
