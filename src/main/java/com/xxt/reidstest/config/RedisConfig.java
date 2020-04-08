package com.xxt.reidstest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.nio.charset.Charset;

@Configuration
public class RedisConfig {

    @Bean
    Jackson2JsonRedisSerializer serializer() {
        return new Jackson2JsonRedisSerializer(Object.class);
    }

    @Bean("redisTemplate")
    RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setDefaultSerializer(serializer());
        return redisTemplate;
    }

    @Bean("stringRedisTemplate")
    StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setDefaultSerializer(new Jackson2JsonRedisSerializer(String.class));
        return redisTemplate;
    }

    /**
     * 一个监听器，代表收到消息之后的处理过程
     * @return
     */
    @Bean
    MessageListener messageListener(){
        return (message,bytes)->{
            System.out.println(new String(message.getBody(), Charset.defaultCharset()));
            System.out.println(new String(message.getChannel(), Charset.defaultCharset()));
            System.out.println(new String(bytes));
        };
    }

    /**
     * messageListener的统一处理容器，可用于注册，注销监听器
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        redisMessageListenerContainer.addMessageListener(messageListener(), new ChannelTopic("channel1122"));

        return redisMessageListenerContainer;
    }


}
