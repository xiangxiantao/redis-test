package com.xxt.reidstest.controller;

import com.xxt.reidstest.service.AppService;
import com.xxt.reidstest.utils.RedisLockUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
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


    /**
     * 利用watch机制实现的cas自增
     *
     * @return
     */
    @GetMapping("/casAdd")
    @ResponseBody
    public String casAdd() {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                int retry = -1;
                List<Object> result = null;
                do {
                    retry++;
                    redisOperations.watch("casIncr2");
                    //这一步必须放在multi之前，否则事务开启之后没有返回值
                    Integer casIncr = (Integer) redisOperations.opsForValue().get("casIncr2");
                    casIncr += 1;
                    redisOperations.multi();
                    redisOperations.opsForValue().set("casIncr2", casIncr);
                    try {
                        result = redisOperations.exec();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //不要一直不断尝试获取锁
                    if (result.size() < 1){
                        try {
                            Random random=new Random();
                            Thread.sleep(random.nextInt(100));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } while (result == null || result.size() < 1);
                System.out.println("重试了：" + retry + "次");
                return result;
            }
        });
        return "ok";
    }

}
