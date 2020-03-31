package com.xxt.reidstest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.Charset;

/**
 * 发布订阅模式
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class PubAndSubTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void publish() {
        stringRedisTemplate.convertAndSend("news.junshi", "msg");
    }

    @Test
    public void sub() throws InterruptedException {
        RedisConnection connection = stringRedisTemplate.getConnectionFactory().getConnection();
        connection.subscribe(new MessageListener() {
            @Override
            public void onMessage(Message message, byte[] bytes) {
                System.out.println(new String(bytes, Charset.defaultCharset()));
            }
        }, "news.*".getBytes());


        Thread.sleep(50000);
    }

}
