package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        log.info("开始创建redis模板对象");
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        //设置redis的连接工厂对象
        template.setConnectionFactory(redisConnectionFactory);
        //设置redis key序列化器，这样key显示就不会乱码
        template.setKeySerializer(new StringRedisSerializer());
        return template;
    }
}
