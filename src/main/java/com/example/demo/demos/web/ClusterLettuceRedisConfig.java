package com.example.demo.demos.web;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.reflect.Method;

/**
 * Cluster 集群配置，含有master slave 切换功能
 */

@Configuration
public class ClusterLettuceRedisConfig extends CachingConfigurerSupport {

    /**
     * 自定义缓存key的生成策略。默认的生成策略是看不懂的(乱码内容) 通过Spring 的依赖注入特性进行自定义的配置注入并且此类是一个配置类可以更多程度的自定义配置
     *
     * @return
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder sb = new StringBuilder();
                sb.append(target.getClass().getName());
                sb.append(method.getName());
                for (Object obj : params) {
                    sb.append(obj.toString());
                }
                return sb.toString();
            }
        };
    }

    /**
     * 缓存配置管理器
     */
    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory factory) {
        //以锁写入的方式创建RedisCacheWriter对象
        RedisCacheWriter writer = RedisCacheWriter.lockingRedisCacheWriter(factory);
        //创建默认缓存配置对象
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        RedisCacheManager cacheManager = new RedisCacheManager(writer, config);
        return cacheManager;
    }


//    /**
//     * redissionClient配置
//     */
//    @Bean
//    public RedissonClient redissonClient(){
//        Config config = new Config();
//        if (redisProperties.getCluster() != null) {
//            //集群模式配置
//            List<String> nodes = redisProperties.getCluster().getNodes();
//            List<String> clusterNodes = new ArrayList<>();
//            for (int i = 0; i < nodes.size(); i++) {
//                clusterNodes.add("redis://" + nodes.get(i));
//            }
//            ClusterServersConfig clusterServersConfig = config.useClusterServers()
//                    .addNodeAddress(clusterNodes.toArray(new String[clusterNodes.size()]));
//            if (!StringUtils.isEmpty(redisProperties.getPassword())) {
//                clusterServersConfig.setPassword(redisProperties.getPassword());
//            }
//        } else {
//            //单节点配置
//            String address = "redis://" + redisProperties.getHost() + ":" + redisProperties.getPort();
//            SingleServerConfig serverConfig = config.useSingleServer();
//            serverConfig.setAddress(address);
//            if (!StringUtils.isEmpty(redisProperties.getPassword())) {
//                serverConfig.setPassword(redisProperties.getPassword());
//            }
//            serverConfig.setDatabase(redisProperties.getDatabase());
//        }
//        //看门狗的锁续期时间，默认30000ms，这里配置成15000ms
//        config.setLockWatchdogTimeout(15000);
//        return Redisson.create(config);
//    }

    // Redis 配置类
    // 自定义的RedisTemplate的Bean名称必须为 redisTemplate。当方法名不为 redisTemplate时，可通过name显示指定bean名称，@Bean(name="redisTemplate")
    @Bean(name = "redisTemplate")
    public RedisTemplate<String,Object> redisTemplate(LettuceConnectionFactory factory){
        RedisTemplate<String,Object> template = new RedisTemplate <>();
        template.setConnectionFactory(factory);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // 在使用注解@Bean返回RedisTemplate的时候，同时配置hashKey与hashValue的序列化方式。
        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);

        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

}
