package com.xxt.reidstest.controller;

import com.xxt.reidstest.service.AppService;
import com.xxt.reidstest.utils.RedisLockUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class AppController {

    public static final String RELEASR_LOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";


    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedisLockUtils redisLockUtils;

    @Autowired
    AppService appService;

    @RequestMapping("/add")
    public String add() {
        System.out.println("ops add");
        Long redID = redisTemplate.opsForValue().increment("redIDstr");
        return redID.toString();
    }

    //不可行，有线程安全问题
    @RequestMapping("/disAdd")
    public String disAdd() {
        String tocken = redisLockUtils.lock("lock", 5, 10, TimeUnit.SECONDS);
        try {
            Integer value = (Integer) redisTemplate.opsForValue().get("distributeId");
            if (value == null) {
                redisTemplate.opsForValue().set("distributeId", 0);
            } else {
                redisTemplate.opsForValue().set("distributeId", ++value);
                System.out.println(value);
            }
        } finally {
            redisLockUtils.release("lock", tocken);
        }
        return "add ok";

    }

    //可行，不够优雅，加锁逻辑和业务逻辑耦合
    @RequestMapping("/disAdd2")
    public String disAdd2() {
        job();
        return "add ok";

    }

    //利用aop环绕通知为业务方法加分布式锁，优雅可行
    @RequestMapping("/disAdd3")
    public String disAdd3() {
        return String.valueOf(appService.simpleJob());

    }

    private void job() {
        String tocken = UUID.randomUUID().toString();
        for (; ; ) {
            if (redisTemplate.opsForValue().setIfAbsent("lock", tocken, 10, TimeUnit.SECONDS)) {

                Integer value = (Integer) redisTemplate.opsForValue().get("betterId");
                if (value == null) {
                    redisTemplate.opsForValue().set("betterId", 0);
                } else {
                    redisTemplate.opsForValue().set("betterId", ++value);
                    System.out.println(value);
                }

                DefaultRedisScript<Boolean> script = new DefaultRedisScript(RELEASR_LOCK_SCRIPT, Boolean.class);
                redisTemplate.execute(script, Arrays.asList("lock"), tocken);
                break;
            }
        }

    }



}
