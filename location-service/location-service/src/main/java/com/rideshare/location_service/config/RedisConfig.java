package com.rideshare.location_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        //String serializer for keys and values
        template.setKeySerializer(template.getStringSerializer());
        template.setValueSerializer(template.getStringSerializer());
        template.setHashKeySerializer(template.getStringSerializer());
        template.setHashValueSerializer(template.getStringSerializer());

        return template;
    }
}
