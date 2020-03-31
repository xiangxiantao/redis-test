package com.xxt.reidstest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PipelineTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 1K:2418ms
     * 10K:3799ms
     * 100k:6422ms
     */
    @Test
    public void testPipeline() {

        long start = System.currentTimeMillis();
        //result接收pipeline中每一条指令的返回值
        List<Object> result = stringRedisTemplate.executePipelined(
                new RedisCallback<Object>() {
                    @Override
                    public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                        StringRedisConnection stringRedisConn = (StringRedisConnection) redisConnection;
                        for (int i = 0; i < 10000; i++) {
                            stringRedisConn.incr("pipekey1");
                        }
                        //返回值必须为null
                        return null;
                    }
                }
        );
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        System.out.println(result.size());
    }


    /**
     * 1K:2772ms
     * 10K:5878ms
     * 10K:27309ms
     */
    @Test
    public void normal() {
        long start = System.currentTimeMillis();
        List<Long> longs=new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            Long aLong = stringRedisTemplate.opsForValue().increment("pipekey2");
            longs.add(aLong);
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }

}
