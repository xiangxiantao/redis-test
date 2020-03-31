package com.xxt.reidstest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    Jackson2JsonRedisSerializer serializer(){
        return new Jackson2JsonRedisSerializer(Object.class);
    }

    @Bean("redisTemplate")
    RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate redisTemplate=new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setDefaultSerializer(serializer());
        return redisTemplate;
    }

    @Bean("stringRedisTemplate")
    StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        StringRedisTemplate redisTemplate=new StringRedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setDefaultSerializer(new Jackson2JsonRedisSerializer(String.class));
        return redisTemplate;
    }

}
