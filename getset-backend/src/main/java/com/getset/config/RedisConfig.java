package com.getset.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Configuration for Caching
 *
 * Configures Redis connection and serialization
 * Spring Boot auto-configures RedisConnectionFactory from application.yml:
 * - spring.redis.host
 * - spring.redis.port
 * - spring.redis.password
 * - spring.redis.timeout
 */
@Configuration
public class RedisConfig {

    /**
     * Configure RedisTemplate with custom serialization
     * Spring Boot will auto-create RedisConnectionFactory from properties
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String serialization for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // Generic JSON serialization for values
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        // Set serializers
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
