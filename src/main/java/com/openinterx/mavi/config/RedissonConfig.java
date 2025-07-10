package com.openinterx.mavi.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password:}") // 如果没有密码，可以留空
    private String redisPassword;

    @Value("${spring.redis.database}")
    private int redisDatabase;

    @Value("${spring.redis.timeout}")
    private int redisTimeout;

    @Bean
    public RedissonClient redissonClient() {
        final Config config = new Config();
        // 配置 Redis 单节点连接
        config.useSingleServer()
                .setAddress(String.format("redis://%s:%d", redisHost, redisPort)) // Redis 地址
                .setPassword(redisPassword.isEmpty() ? null : redisPassword)      // Redis 密码
                .setDatabase(redisDatabase)                                       // Redis 数据库
                .setTimeout(redisTimeout)
                .setConnectionPoolSize(64)  // 设置最大连接池大小
                .setConnectionMinimumIdleSize(10);  // 设置最小空闲连接数;
                // 超时时间
        config.setCodec(new JsonJacksonCodec());
        return Redisson.create(config);
    }
}
