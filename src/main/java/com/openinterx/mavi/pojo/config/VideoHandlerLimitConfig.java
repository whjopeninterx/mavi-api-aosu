package com.openinterx.mavi.pojo.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoHandlerLimitConfig {
    private  int secondsRateLimit = 10; // 每秒中
    private int dailyRateLimit = 1000; // 每天最多 1000 次提交
    private int secondsWindow = 1; // 时间窗口（分钟）
    private  int idleTtl = 600; // 空闲过期时间（秒）
}
