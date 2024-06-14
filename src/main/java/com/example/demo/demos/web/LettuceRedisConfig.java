package com.example.demo.demos.web;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import io.lettuce.core.ReadFrom;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * springboot lettuce集成redis
 */
@Configuration
public class LettuceRedisConfig {
    @Value("${spring.redis.sentinel.master}")
    private String masterName;

    @Value("${spring.redis.sentinel.password}")
    private String password;

    @Value("${spring.redis.sentinel.nodes}")
    private String nodes;

//    @Value("${spring.redis.sentinel.username}")
//    private String username;

    @Value("${spring.redis.lettuce.pool.max-total}")
    private Integer maxTotal;

    @Value("${spring.redis.lettuce.pool.max-wait}")
    private Integer maxWait;

    @Value("${spring.redis.lettuce.pool.max-idle}")
    private Integer maxIdle;

    @Value("${spring.redis.lettuce.pool.min-idle}")
    private Integer minIdle;


    @Bean(name="lettuceConnectionFactory")
    public RedisConnectionFactory lettuceConnectionFactory() {

        // 读取sentinel配置
        List<String> sentinelNodeList = Arrays.asList(nodes.split(","));
        RedisSentinelConfiguration sentinelConfiguration = new RedisSentinelConfiguration(masterName, new HashSet<>(sentinelNodeList));

        // 连接池配置
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        // 线程池线程最大空闲数
        genericObjectPoolConfig.setMaxIdle(maxIdle);
        // 线程池线程最小空闲数
        genericObjectPoolConfig.setMinIdle(minIdle);
        // 线程池最大线程数
        genericObjectPoolConfig.setMaxTotal(maxTotal);
        // 当连接池已用完时，客户端应该等待获取新连接的最大时间，单位为毫秒
        genericObjectPoolConfig.setMaxWaitMillis(maxWait);


        // 设置节点和哨兵访问密码
        sentinelConfiguration.setPassword(RedisPassword.of(password));
        sentinelConfiguration.setSentinelPassword(RedisPassword.of(password));
        sentinelConfiguration.setDatabase(0);

        // lettuce client配置，在进行读取时，访问任意一个节点，默认读写都只从主节点进行
        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration
                .builder().readFrom(ReadFrom.ANY).poolConfig(genericObjectPoolConfig).build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(sentinelConfiguration, clientConfig);
        // 在获取连接时，先验证连接是否已经中断，如果已经中断则创建一个新的连接
        factory.setValidateConnection(true);
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("lettuceConnectionFactory") RedisConnectionFactory connectionFactory) {
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

//    @Bean("lettuceConnectionFactory")
//    public RedisConnectionFactory lettuceConnectionFactory(RedisProperties redisProperties) {
//
//        // 读取sentinel配置
//        String masterName = redisProperties.getSentinel().getMaster();
//        List<String> sentinelNodeList = redisProperties.getSentinel().getNodes();
//        RedisSentinelConfiguration sentinelConfiguration = new RedisSentinelConfiguration(masterName, new HashSet<>(sentinelNodeList));
//
//        // 设置节点和哨兵访问密码
//        sentinelConfiguration.setPassword(redisProperties.getPassword());
//        sentinelConfiguration.setSentinelPassword(redisProperties.getPassword());
//
//        // lettuce client配置，在进行读取时，访问任意一个节点，默认读写都只从主节点进行
//        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration
//                .builder().readFrom(ReadFrom.ANY).build();
//
//        LettuceConnectionFactory factory = new LettuceConnectionFactory(sentinelConfiguration, clientConfig);
//        // 在获取连接时，先验证连接是否已经中断，如果已经中断则创建一个新的连接
//        factory.setValidateConnection(true);
//        return factory;
//    }

//    @Bean
//    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory connectionFactory) {
//        return new StringRedisTemplate(connectionFactory);
//    }
}
