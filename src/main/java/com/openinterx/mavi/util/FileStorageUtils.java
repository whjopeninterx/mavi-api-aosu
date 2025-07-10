package com.openinterx.mavi.util;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.openinterx.mavi.config.NacosBaseConfig;
import com.openinterx.mavi.exception.XvuException;
import com.openinterx.mavi.pojo.config.GcsConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
@Slf4j
public class FileStorageUtils {

    public static UploadResponse gcs_upload(InputStream inputStream, String key, String path) {
        final NacosBaseConfig bean = ApplicationContextUtil.getBean(NacosBaseConfig.class);
        final GcsConfig config = JsonUtils.strToObj(bean.getGcsConfig(), GcsConfig.class);
        final UploadResponse uploadResponse = new UploadResponse();
        final Storage storage = ApplicationContextUtil.getBean(Storage.class);
        try {
            log.info("gcs_upload objectName: {}", key);
            final BlobId blobId = BlobId.of(config.getBucket(), path + "/" + key);
            final BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            final Blob blob = storage.create(blobInfo, inputStream);
            log.info("gcs_upload success: {}", blob.getName());
            uploadResponse.setUuid(key);
            uploadResponse.setUrl("gs://"+config.getBucket()+"/"+path+"/"+blob.getName());
            uploadResponse.setBucket(blobInfo.getBucket());
            uploadResponse.setSize(blob.getSize());
        } catch (Exception e) {
            throw new XvuException("gcs_upload is error!", e);
        }
        return uploadResponse;

    }

    @Getter
    @Setter
    public static class UploadResponse {

        private String uuid;
        private String url;
        private Long duration;
        private String bucket;
        private Long size;
    }

}
