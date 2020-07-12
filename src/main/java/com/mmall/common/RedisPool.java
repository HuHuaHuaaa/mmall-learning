package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool {
    private static JedisPool pool;//jedis连接池
    private static Integer maxTotal= Integer.parseInt(PropertiesUtil.getProperty("redis.max,total","20"));//最大连接数
    private static Integer maxIdle= Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle","10")); //在jedispool中最大的idle状态（空闲的）的jedis实例个数
    private static Integer minIdle= Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle","2"));//在jedispool中最小的idle状态（空闲的）的jedis实例个数

    private static Boolean testOnBorrow= Boolean.parseBoolean(PropertiesUtil.getProperty("reids.test.borrow","true"));//在borrow一个jedis实例的时候，是否进行验证操作，如果赋值为true，则得到的jedis实例肯定是可以用的
    private static Boolean testOnReturn= Boolean.parseBoolean(PropertiesUtil.getProperty("reids.test.return","true"));//在return一个jedis实例的时候，是否进行验证操作，如果赋值为true，则放回jedispool的jedis实例肯定是可以用的

    private static String  redisip=PropertiesUtil.getProperty("redis.ip");
    private static Integer redisport= Integer.parseInt(PropertiesUtil.getProperty("redis.port"));

    private static void initPool(){
        JedisPoolConfig config=new JedisPoolConfig();

        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        config.setBlockWhenExhausted(true);

        pool =new JedisPool(config,redisip,redisport,1000*2);
    }

    static{
        initPool();
    }

    public static Jedis getJedis(){
        return pool.getResource();
    }

    public static void returnBrokenResource(Jedis jedis){
        pool.returnBrokenResource(jedis);
    }

    public static void returnResource(Jedis jedis){
        pool.returnResource(jedis);
    }

    public static void main(String[] args) {
        Jedis jedis=pool.getResource();
        jedis.set("silencekey","silencevalue");
        returnResource(jedis);

        pool.destroy();
        System.out.println("program is end");
    }


}
