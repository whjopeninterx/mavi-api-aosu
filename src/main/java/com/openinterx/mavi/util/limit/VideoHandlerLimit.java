package com.openinterx.mavi.util;

import com.openinterx.mavi.pojo.config.VideoHandlerLimitConfig;
import org.redisson.api.*;

import java.util.concurrent.TimeUnit;

public class VideoHandlerLimit {


    private static  int MINUTE_RATE_LIMIT ; // 每分钟最多 10 次提交
    private static  int DAILY_RATE_LIMIT ; // 每天最多 1000 次提交
    private static  long MINUTE_WINDOW ; // 时间窗口（分钟）
    private static  long IDLE_TTL ; // 空闲过期时间（秒）

    public VideoHandlerLimit(VideoHandlerLimitConfig config) {
        MINUTE_RATE_LIMIT=config.getMinuteRateLimit();
        DAILY_RATE_LIMIT=config.getDailyRateLimit();
        MINUTE_WINDOW=config.getMinuteWindow();
        IDLE_TTL=config.getIdleTtl();
    }

    public boolean isAllowed(String key, String account) {
        // 每分钟限制的令牌桶
        account= MD5Utils.string2MD5(account);
        RedissonClient redissonClient= ApplicationContextUtil.getBean(RedissonClient.class);
        String minuteRateLimiterKey = key  + account;
        RRateLimiter minuteRateLimiter = redissonClient.getRateLimiter(minuteRateLimiterKey);

        // 使用当前日期来区分每日请求
        long currentDate = System.currentTimeMillis() / 86400000; // 获取当前日期的天数
        String dailyCounterKey = key + "dailyCounter:" + account + ":" + currentDate; // 使用日期区分每天

        RMap<String, Long> dailyCounter = redissonClient.getMap(dailyCounterKey);

        // 配置每分钟的令牌桶，如果不存在则初始化
        if (!minuteRateLimiter.isExists()) {
            minuteRateLimiter.trySetRate(RateType.OVERALL, MINUTE_RATE_LIMIT, MINUTE_WINDOW, RateIntervalUnit.MINUTES);
        }

        // 尝试获取每分钟的令牌
        boolean minuteAllowed = minuteRateLimiter.tryAcquire();

        // 如果没有通过每分钟限制，则拒绝请求
        if (!minuteAllowed) {
            return false;
        }
        minuteRateLimiter.expire(IDLE_TTL, TimeUnit.SECONDS); // 设置空闲过期时间
        // 检查每天的限制
        Long currentCount = dailyCounter.get(account);
        if (currentCount == null || currentCount < 0) {
            // 计数器为空或当天没有请求，初始化为 0
            dailyCounter.put(account, 0L);
        }

        long dailyLimitCount = dailyCounter.get(account);

        if (dailyLimitCount < DAILY_RATE_LIMIT) {
            // 如果每天的限制没有到达最大值，则更新计数器并允许请求
            dailyCounter.put(account, dailyLimitCount + 1);

            // 设置每天计数器的过期时间为 24 小时，确保零点时清空
            dailyCounter.expireAt(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));

            return true;
        } else {
            // 如果超过每日限制，则拒绝请求
            return false;
        }
    }
}
