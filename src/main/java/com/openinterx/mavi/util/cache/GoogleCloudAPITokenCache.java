package com.openinterx.mavi.util.cache;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.openinterx.mavi.config.NacosBaseConfig;
import com.openinterx.mavi.pojo.config.GcsConfig;
import com.openinterx.mavi.util.ApplicationContextUtil;
import com.openinterx.mavi.util.JsonUtils;
import com.openinterx.mavi.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
@Slf4j
public class GoogleCloudAPITokenCache {
    public final static String GOOGLE_API_TOKEN = "GOOGLEAPI:TOKEN";

    public static String getGoogleApiToken() {
        final NacosBaseConfig nacosConfiguration = ApplicationContextUtil.getBean(NacosBaseConfig.class);
        final RedissonUtil redissonUtil = ApplicationContextUtil.getBean(RedissonUtil.class);
        final GcsConfig config = JsonUtils.strToObj(nacosConfiguration.getGcsConfig(), GcsConfig.class);
        final String key =GOOGLE_API_TOKEN + config.getBucket();
        if (!redissonUtil.hasCache(key)) {
            try {
                final String osName = System.getProperty("os.name").toLowerCase();
                InputStream inputStream;
                if(osName.contains("win")){
                    inputStream=Files.newInputStream(Paths.get("D:\\google_private_key\\gen-lang-client-0057517563-afc1612f5545.json"));
                     }  else {
                    inputStream=Files.newInputStream(Paths.get(config.getAuthJsonPath()));
                }
                final GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream)
                        .createScoped(config.getScoped());

                // 刷新 token（如果必要）并获取访问 token
                credentials.refreshIfExpired();
                final AccessToken accessToken = credentials.getAccessToken();
                redissonUtil.setCacheT(key, accessToken.getTokenValue(), accessToken.getExpirationTime().getTime() - System.currentTimeMillis() - 60000L, TimeUnit.MILLISECONDS);

            } catch (Exception e) {
                log.error("get google cloud api token error", e);
                getGoogleApiToken();
            }
        }
        return JsonUtils.objToStr(redissonUtil.getCache(key));
    }

}
