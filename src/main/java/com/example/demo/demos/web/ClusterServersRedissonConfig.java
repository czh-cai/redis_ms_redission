package com.example.demo.demos.web;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.MasterSlaveServersConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * cluster 集群胚子(key 分片)
 */
@Configuration
public class ClusterServersRedissonConfig {

    @Bean("redissonClient")
    public RedissonClient redissonClient() {

        Config config = new Config();

        // 设置主节点信息
        ClusterServersConfig serverConfig = config.useClusterServers()
                // 集群状态扫描间隔时间，单位是毫秒
                .setScanInterval(2000)
                //可以用"rediss://"来启用SSL连接
                .addNodeAddress("redis://192.168.0.231:6380", "redis://192.168.0.231:6381")
                .addNodeAddress("redis://192.168.0.232:6382", "redis://192.168.0.232:6383")
                .addNodeAddress("redis://192.168.0.233:6384", "redis://192.168.0.233:6385")
                // 主节点密码
                .setPassword("123123");
        
        // 创建RedissonClient实例
        RedissonClient redissonClient = Redisson.create(config);

        return redissonClient;
    }
}
