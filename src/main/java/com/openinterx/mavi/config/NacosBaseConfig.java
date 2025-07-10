package com.openinterx.mavi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@RefreshScope
public class NacosConfig {

    @Value("${understand.limit.config}")
    private String transcriptionLimitConfig;

    @Value(("${gcs.config}"))
    private String gcsConfig;

}
