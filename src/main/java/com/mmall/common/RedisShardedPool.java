package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

public class RedisShardedPool {
    private static ShardedJedisPool pool;//sharded jedis连接池
    private static Integer maxTotal= Integer.parseInt(PropertiesUtil.getProperty("redis.max,total","20"));//最大连接数
    private static Integer maxIdle= Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle","10")); //在jedispool中最大的idle状态（空闲的）的jedis实例个数
    private static Integer minIdle= Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle","2"));//在jedispool中最小的idle状态（空闲的）的jedis实例个数

    private static Boolean testOnBorrow= Boolean.parseBoolean(PropertiesUtil.getProperty("reids.test.borrow","true"));//在borrow一个jedis实例的时候，是否进行验证操作，如果赋值为true，则得到的jedis实例肯定是可以用的
    private static Boolean testOnReturn= Boolean.parseBoolean(PropertiesUtil.getProperty("reids.test.return","true"));//在return一个jedis实例的时候，是否进行验证操作，如果赋值为true，则放回jedispool的jedis实例肯定是可以用的

    private static String  redis1ip=PropertiesUtil.getProperty("redis1.ip");
    private static Integer redis1port= Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));
    private static String  redis2ip=PropertiesUtil.getProperty("redis2.ip");
    private static Integer redis2port= Integer.parseInt(PropertiesUtil.getProperty("redis2.port"));

    private static void initPool(){
        JedisPoolConfig config=new JedisPoolConfig();

        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        config.setBlockWhenExhausted(true);

        JedisShardInfo info1 =new JedisShardInfo(redis1ip,redis1port,1000*2);
        JedisShardInfo info2 =new JedisShardInfo(redis2ip,redis2port,1000*2);

        List<JedisShardInfo> jedisShardInfoList = new ArrayList<JedisShardInfo>(2);

        jedisShardInfoList.add(info1);
        jedisShardInfoList.add(info2);

        pool =new ShardedJedisPool(config,jedisShardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);
    }

    static{
        initPool();
    }

    public static ShardedJedis getJedis(){
        return pool.getResource();
    }

    public static void returnBrokenResource(ShardedJedis jedis){
        pool.returnBrokenResource(jedis);
    }

    public static void returnResource(ShardedJedis jedis){
        pool.returnResource(jedis);
    }

    public static void main(String[] args) {
        ShardedJedis jedis=pool.getResource();

        for(int i=0;i<10;i++){
            jedis.set("key"+i,"value"+i);
        }


        returnResource(jedis);

//        pool.destroy();
        System.out.println("program is end");
    }
}
