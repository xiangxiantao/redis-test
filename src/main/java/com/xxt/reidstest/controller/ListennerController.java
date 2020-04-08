package com.xxt.reidstest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ListennerController {

    @Autowired
    RedisMessageListenerContainer redisMessageListenerContainer;

    @Autowired
    MessageListener messageListener;

    /**
     * 动态的添加一个redis监听器，实时生效
     * @param channelName
     * @return
     */
    @GetMapping("/addLi/{channelName}")
    public String addChannel(@PathVariable String channelName) {
        System.out.println("添加监听器：" + channelName);
        redisMessageListenerContainer.addMessageListener(messageListener, new ChannelTopic(channelName));
        return "add " + channelName + "ok";
    }

}
