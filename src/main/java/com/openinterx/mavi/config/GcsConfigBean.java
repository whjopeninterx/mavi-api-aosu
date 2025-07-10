package com.openinterx.mavi.config;

import com.google.api.services.storage.Storage;
import com.openinterx.mavi.util.ApplicationContextUtil;
import org.springframework.context.annotation.Bean;

import java.nio.file.Files;
import java.nio.file.Paths;

public class GcsConfig {

    @Bean
    public Storage storage() {
        final NacosBaseConfig nacosBaseConfig = ApplicationContextUtil.getBean(NacosBaseConfig.class);
        final GoogleCloudConfigInfo googleCloudConfigInfo = JsonUtils.strToObj(nacosConfiguration.getGoogleCloudConfig(), GoogleCloudConfigInfo.class);
        try {
            // "D:\\google_private_key\\gen-lang-client-0057517563-afc1612f5545.json"
            final String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                return StorageOptions.newBuilder()
                        .setCredentials(GoogleCredentials.fromStream(Files.newInputStream(Paths.get("D:\\google_private_key\\gen-lang-client-0057517563-afc1612f5545.json"))))
                        .build()
                        .getService();
            } else {
                return StorageOptions.newBuilder()
                        .setCredentials(GoogleCredentials.fromStream(Files.newInputStream(Paths.get(googleCloudConfigInfo.getAuthJsonPath()))))
                        .build()
                        .getService();
            }

        } catch (Exception e) {
            throw new XvuException("storage 全局异常！", e);
        }
    }
}
