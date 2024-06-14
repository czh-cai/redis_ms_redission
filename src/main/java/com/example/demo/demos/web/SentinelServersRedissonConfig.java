package com.example.demo.demos.web;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.MasterSlaveServersConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * sentinel master slave config
 */
@Configuration
public class SentinelServersRedissonConfig {

    @Bean("redissonSentinelClient")
    public RedissonClient redissonClient() {

        Config config = new Config();
        // sentinel 节点
        config.useSentinelServers()
                .addSentinelAddress("redis://192.168.0.231:26379",
                        "redis://192.168.0.231:36379", "redis://192.168.0.231:46379",
                        "redis://192.168.0.232:26379", "redis://192.168.0.232:36379",
                        "redis://192.168.0.233:26379", "redis://192.168.0.233:36379")
                .setMasterName("mymaster")
                .setPassword("123456")
                .setDatabase(0)
                .setMasterConnectionPoolSize(50) // 连接 master 的连接池大小
                .setSlaveConnectionPoolSize(50)// 连接 slave 的连接池大小
                .setSlaveConnectionMinimumIdleSize(10);

        // 创建RedissonClient实例
        RedissonClient redissonClient = Redisson.create(config);

        return redissonClient;
    }
}
