package com.openinterx.mavi.util;

import org.redisson.api.*;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

public class RedissonUtil {




    private static final RedissonClient redissonClient;
    static {
        redissonClient=ApplicationContextUtil.getBean(RedissonClient.class);
    }

    // 缓存功能
    public void setCacheT(String key, Object value, long timeout, TimeUnit timeUnit) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value, timeout, timeUnit);
    }

    /**
     * @param key
     * @param value
     */
    public void setCache(String key, Object value) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }

    public Object getCache(String key) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    public void deleteCache(String key) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.delete();
    }

    public void batchDeleteCache(String prefixKey){
        final RKeys keys = redissonClient.getKeys();
        keys.deleteByPattern(prefixKey);
    }

    public boolean hasCache(String key) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        return bucket.isExists();
    }

    // 分布式锁功能
    public boolean tryLock(String lockKey, long leaseTime, long waitTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }


    public void updateCache(String key, Object value) {
        // 假设你有一个 RedissonClient 实例
        RBucket<Object> bucket = redissonClient.getBucket(key);
        // 1. 获取当前键的过期时间（秒）
        Long ttl = bucket.remainTimeToLive(); // 返回的单位是毫秒，如果没有设置过期时间，返回 -1
        // 2. 设置新的值
        if(ttl>0){
            bucket.set(value,ttl,TimeUnit.MILLISECONDS);
        }else {
            bucket.set(value);
        }
    }
    public long incr(String key){
        RAtomicLong counter = redissonClient.getAtomicLong(key);
        counter.expire(30, TimeUnit.MINUTES);
        return counter.incrementAndGet();
    }
}
