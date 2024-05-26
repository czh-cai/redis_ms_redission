package com.example.demo.demos.web;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * springboot lettuce集成redis
 */
@Configuration
public class LettuceRedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        // 创建一个新的RedisTemplate实例，用于操作Redis
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
        // 设置RedisTemplate使用的连接工厂，以便它能够连接到Redis服务器
        redisTemplate.setConnectionFactory(connectionFactory);

        // 创建一个StringRedisSerializer实例，用于序列化Redis的key为字符串
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // 创建一个Jackson2JsonRedisSerializer实例，用于序列化Redis的value为JSON格式
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        // 创建一个ObjectMapper实例，用于处理JSON的序列化和反序列化
        ObjectMapper objectMapper = new ObjectMapper();

        // 设置ObjectMapper的属性访问级别，以便能够序列化对象的所有属性
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 启用默认的类型信息，以便在反序列化时能够知道对象的实际类型
        // 注意：这里使用了新的方法替换了过期的enableDefaultTyping方法
        // 方法过期，改为下面代码
        // objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        // 设置Jackson2JsonRedisSerializer使用的ObjectMapper
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        // 设置RedisTemplate的key序列化器为stringRedisSerializer
        redisTemplate.setKeySerializer(stringRedisSerializer); // key的序列化类型
        // 设置RedisTemplate的value序列化器为jackson2JsonRedisSerializer
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer); // value的序列化类型

        // 设置RedisTemplate的hash key序列化器为stringRedisSerializer
        redisTemplate.setHashKeySerializer(stringRedisSerializer);  // key的序列化类型
        // 设置RedisTemplate的hash value序列化器为jackson2JsonRedisSerializer
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);   // value的序列化类型

        // 调用RedisTemplate的afterPropertiesSet方法，该方法会执行一些初始化操作，比如检查序列化器是否设置等
        redisTemplate.afterPropertiesSet();

        // 返回配置好的RedisTemplate实例
        return redisTemplate;
    }
}
