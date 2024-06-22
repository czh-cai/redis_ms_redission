package com.example.demo.demos.web;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.MasterSlaveServersConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 主从配置
 */
//@Configuration
public class MasterSlaveServersRedissonConfig {

//    @Bean("redissonClient")
    public RedissonClient redissonClient() {

        Config config = new Config();

        // 设置主节点信息
        MasterSlaveServersConfig serverConfig = config.useMasterSlaveServers()
                // 主节点地址和端口
                .setMasterAddress("redis://192.168.0.231:6379")
                // 主节点密码
                .setPassword("123456")
                // 数据库实例
                .setDatabase(0);

        // 可以添加一个或多个从节点
        serverConfig.addSlaveAddress("redis://192.168.0.232:6379",
                "redis://192.168.0.233:6379");

        // 创建RedissonClient实例
        RedissonClient redissonClient = Redisson.create(config);

        return redissonClient;
    }

}
