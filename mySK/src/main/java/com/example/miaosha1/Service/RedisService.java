package com.example.miaosha1.Service;

import com.alibaba.fastjson.JSON;
import com.example.miaosha1.redis.KeyPrefix;
import com.example.miaosha1.redis.OrderKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.ArrayList;
import java.util.List;

@Service
public class RedisService {
    @Autowired
    JedisPool jedisPool;

    //获取对象
    public <T> T get(KeyPrefix prefix,String key,Class<T> clazz){
        Jedis jedis = null;

        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix()+key;
            String str = jedis.get(realKey);
            T t = stringToBean(str,clazz);//redis中存的是String，取出redis的value后，需要反序列化为bean

            return t;
        }finally {
            returnToPool(jedis);
        }
    }

    //方法中包含泛型时，在返回值前面加上泛型<T>
    //添加对象
    public <T> boolean set(KeyPrefix prefix, String key, T value){
        Jedis jedis = null;

        try {
            jedis = jedisPool.getResource();
            String str = beanToString(value);//java中是bean对象，需要序列化为String，再存入redis
            if(str==null||str.length()==0)
                return false;
            //生成真正的key
            String realKey = prefix.getPrefix()+key;
            System.out.println(realKey);
            int seconds = prefix.getExpireSeconds();
            if(seconds<=0){
                jedis.set(realKey,str);//该key没有过期时间
            }else{
                jedis.setex(realKey,seconds,str);//需要设置过期时间
            }
            return true;
        }finally {
            returnToPool(jedis);
        }
    }

    //判断key是否存在
    public <T> boolean exists(KeyPrefix prefix, String key){
        Jedis jedis = null;

        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix()+key;
            return jedis.exists(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    //判断key是否存在
    public boolean delete(KeyPrefix prefix, String key){
        Jedis jedis = null;

        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix()+key;
            long res = jedis.del(realKey);
            return res>0;
        }finally {
            returnToPool(jedis);
        }
    }

    //key增加
    public <T> Long incr(KeyPrefix prefix, String key){
        Jedis jedis = null;

        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix()+key;
            return jedis.incr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    //key减小
    public <T> Long decr(KeyPrefix prefix, String key){
        Jedis jedis = null;

        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix()+key;
            return jedis.decr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    //序列化
    public static <T>String beanToString(T value) {
        if(value==null)
            return null;

        Class<?> clazz = value.getClass();

        if(clazz == int.class || clazz == Integer.class){
            return ""+value;
        }else if(clazz == long.class || clazz == Long.class){
            return ""+value;
        }else if(clazz == String.class){
            return (String) value;
        }else{
            return JSON.toJSONString(value);
        }
    }

    //反序列化
    public static <T>T stringToBean(String str,Class<T> clazz) {
        if(str == null || str.length()==0){
            return null;
        }

        if(clazz == int.class || clazz == Integer.class){
            return (T) Integer.valueOf(str);
        }else if(clazz == long.class || clazz == Long.class){
            return (T) Long.valueOf(str);
        }else if(clazz == String.class){
            return (T) str;
        }else{
            return JSON.toJavaObject(JSON.parseObject(str),clazz);
        }

    }

    private void returnToPool(Jedis jedis) {
        if(jedis!=null){
            jedis.close();
        }
    }

    public boolean delete(KeyPrefix prefix) {
        if(prefix == null) {
            return false;
        }
        List<String> keys = scanKeys(prefix.getPrefix());
        if(keys==null || keys.size() <= 0) {
            return true;
        }
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.del(keys.toArray(new String[0]));
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if(jedis != null) {
                jedis.close();
            }
        }
    }
    public List<String> scanKeys(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            List<String> keys = new ArrayList<String>();
            String cursor = "0";
            ScanParams sp = new ScanParams();
            sp.match("*"+key+"*");
            sp.count(100);
            do{
                ScanResult<String> ret = jedis.scan(cursor, sp);
                List<String> result = ret.getResult();
                if(result!=null && result.size() > 0){
                    keys.addAll(result);
                }
                //再处理cursor
                cursor = ret.getCursor();
            }while(!cursor.equals("0"));
            return keys;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
