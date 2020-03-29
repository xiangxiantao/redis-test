package com.xxt.reidstest.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Aspect
@Component
public class RedisLockAop {

    public static final String RELEASR_LOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    @Autowired
    RedisTemplate redisTemplate;

    @Pointcut("@annotation(com.xxt.reidstest.anntation.RedisLock)")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Integer lockHandler(ProceedingJoinPoint joinPoint) {
        String tocken = UUID.randomUUID().toString();
        Integer result =null;
        for (; ; ) {
            if (redisTemplate.opsForValue().setIfAbsent("lock", tocken, 10, TimeUnit.SECONDS)) {
                System.out.println("获取锁成功：" + tocken);
                try {
                    result = (Integer) joinPoint.proceed();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    System.out.println("执行业务失败");
                }

                DefaultRedisScript<Boolean> script = new DefaultRedisScript(RELEASR_LOCK_SCRIPT, Boolean.class);
                Boolean state = (Boolean) redisTemplate.execute(script, Arrays.asList("lock"), tocken);
                String res = state ? "成功" : "失败";
                System.out.println("释放锁：" + tocken + res);
                break;
            }
        }
        return result;
    }

}
