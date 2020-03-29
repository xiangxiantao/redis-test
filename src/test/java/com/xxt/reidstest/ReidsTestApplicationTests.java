package com.xxt.reidstest;

import com.xxt.reidstest.utils.RedisLockUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
class ReidsTestApplicationTests {

    @Autowired
    private RedisLockUtils redisLockUtils;

    @Test
    void contextLoads() {
    }

    @Test
    void testGetLock(){
        String lock = redisLockUtils.lock("lock", 300, 6, TimeUnit.SECONDS);
        System.out.println(lock);
    }

    @Test
    void testReleaseLock(){
        Boolean state = redisLockUtils.release("lock", "706b5755-056a-49e6-a80f-158b74878cae");
        System.out.println(state);
    }
}
