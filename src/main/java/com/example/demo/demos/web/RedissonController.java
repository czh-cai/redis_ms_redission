package com.example.demo.demos.web;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import com.alibaba.fastjson2.JSON;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/main/redisson")
@Slf4j
public class RedissonController {
    @Autowired
    @Qualifier(value="redissonClient")
    private RedissonClient redissonClient;
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/get/{hashEntryKey}")
    @ResponseBody
    public String get(@PathVariable("hashEntryKey") Object hashEntryKey) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        String value = hashOperations.get("BUS_HS", String.valueOf(hashEntryKey));

        if (StringUtils.isNotBlank(value)) {
            String lockKey = "lock_key_001";
            RLock lock = redissonClient.getLock(lockKey);
            try {
                boolean res = lock.tryLock(10, TimeUnit.SECONDS);
                if (res) {
                    value = hashOperations.get("BUS_HS", String.valueOf(hashEntryKey));
                    if (StringUtils.isNotBlank(value)) {
                        log.info("读取数据库数据....更新新的缓存");
                        Object newValue = "456";
                        hashOperations.put("BUS_HS", String.valueOf(hashEntryKey), JSON.toJSONString(newValue));
                    }
                } else  {
                  log.warn("系统繁忙........");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error("读取数据异常");
            } finally {
                lock.unlock();
            }
        } else {
            log.info("从缓存中读取:{}",value);
        }
        value = hashOperations.get("BUS_HS", String.valueOf(hashEntryKey));
        return value;
    }

    @GetMapping("/set")
    @ResponseBody
    public String set(@RequestParam("hashEntryKey") String hashEntryKey,@RequestParam("hashEntryValue") String hashEntryValue) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        String cacheKey = "BUS_HS";
        String cacheValue = hashOperations.get(cacheKey, String.valueOf(hashEntryKey));

        String lockKey = "lock_key_001";
        RLock lock = redissonClient.getLock(lockKey);

        try {

            // 具有Watch Dog 自动延期机制 默认续30s 每隔30/3=10 秒续到30s (每10s续约一次)
            lock.lock();

            //设置了时间就没有了watch dog
            //lock.lock(30, TimeUnit.SECONDS);
            //Thread.sleep(8 * 10 * 1000);
            cacheValue = hashOperations.get(cacheKey, String.valueOf(hashEntryKey));
            log.info("缓存旧值:{}",cacheValue);

            log.info("取得锁操作数据库更新....更新");
            hashOperations.put(cacheKey, String.valueOf(hashEntryKey), String.valueOf(hashEntryValue));
            hashEntryValue = hashOperations.get(cacheKey, String.valueOf(hashEntryKey));

        } catch (Exception ex) {
            log.error("获取所超时！", ex);
        } finally {
            lock.unlock();
        }

        return hashEntryValue;
    }

}
