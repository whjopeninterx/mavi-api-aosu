package com.openinterx.mavi.config;


import com.google.auth.oauth2.GoogleCredentials;
import com.openinterx.mavi.exception.XvuException;
import com.openinterx.mavi.pojo.config.GcsConfig;
import com.openinterx.mavi.util.ApplicationContextUtil;
import com.openinterx.mavi.util.JsonUtils;
import org.springframework.context.annotation.Bean;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Paths;
@Configuration
public class GcsConfigBean {

    @Bean
    public Storage storage() {
        try {
            final NacosBaseConfig nacosBaseConfig = ApplicationContextUtil.getBean(NacosBaseConfig.class);
            final GcsConfig config = JsonUtils.strToObj(nacosBaseConfig.getGcsConfig(), GcsConfig.class);
            final String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                return StorageOptions.newBuilder()
                        .setCredentials(GoogleCredentials.fromStream(Files.newInputStream(Paths.get("D:\\google_private_key\\gen-lang-client-0057517563-afc1612f5545.json"))))
                        .build()
                        .getService();
            } else {
                return StorageOptions.newBuilder()
                        .setCredentials(GoogleCredentials.fromStream(Files.newInputStream(Paths.get(config.getAuthJsonPath()))))
                        .build()
                        .getService();
            }

        } catch (Exception e) {
            throw new XvuException("storage 全局异常！", e);
        }
    }
}
