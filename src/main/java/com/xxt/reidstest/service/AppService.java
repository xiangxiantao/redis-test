package com.xxt.reidstest.service;

import com.xxt.reidstest.anntation.RedisLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AppService {

    @Autowired
    RedisTemplate redisTemplate;

    @RedisLock
    public Integer simpleJob(){
        Integer value = (Integer) redisTemplate.opsForValue().get("aopID");
        if (value == null) {
            redisTemplate.opsForValue().set("aopID", 0);
        } else {
            redisTemplate.opsForValue().set("aopID", ++value);
            System.out.println(value);
        }
        return value;
    }
}
