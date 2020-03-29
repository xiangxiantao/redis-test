package com.xxt.reidstest.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁的工具类
 */
@Component
public class RedisLockUtils {

    public static final String RELEASR_LOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 获取一个redis锁
     * 在等待时间之内自旋获取锁，保证在超时时间之内一定拿到锁，否则抛出异常
     * @param lockName 锁名称
     * @param time     等待获取锁的时间
     * @param timeUnit 时间单位
     * @return 当前拥有锁的tocken
     */
    public String lock(String lockName, long expired, long time, TimeUnit timeUnit) {
        long startTimes = System.currentTimeMillis();
        String tocken = UUID.randomUUID().toString();
        do {
            if (timeUnit.convert(System.currentTimeMillis() - startTimes, TimeUnit.MILLISECONDS) > time) {
                throw new RuntimeException("获取锁超时");
            }
            if (redisTemplate.opsForValue().setIfAbsent(lockName, tocken, expired, TimeUnit.SECONDS)) {
                break;
            }
        } while (true);

        return tocken;
    }

    /**
     * 释放锁
     * execute方法的返回值只支持以下几种类型
     * if (javaType == null) {
     * return ReturnType.STATUS;
     * }
     * if (javaType.isAssignableFrom(List.class)) {
     * return ReturnType.MULTI;
     * }
     * if (javaType.isAssignableFrom(Boolean.class)) {
     * return ReturnType.BOOLEAN;
     * }
     * if (javaType.isAssignableFrom(Long.class)) {
     * return ReturnType.INTEGER;
     * }
     *
     * @param lockName
     * @param tocken
     * @return
     */
    public Boolean release(String lockName, String tocken) {
        DefaultRedisScript<Boolean> script = new DefaultRedisScript(RELEASR_LOCK_SCRIPT, Boolean.class);
        return (Boolean) redisTemplate.execute(script, Arrays.asList(lockName), tocken);
    }
}
