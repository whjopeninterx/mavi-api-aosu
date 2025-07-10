package com.openinterx.mavi.util;

import com.openinterx.mavi.exception.ValidateException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class FileFetcher {

    private static final RestTemplate restTemplate = new RestTemplate();

    public static RemoteFileMeta  getRemoteFileMeta(String url) {
       try {
           final HttpHeaders headers = new HttpHeaders();
           final HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
           final ResponseEntity<Void> response = restTemplate.exchange(
                   url,
                   HttpMethod.HEAD,
                   requestEntity,
                   Void.class
           );
           final HttpHeaders responseHeaders = response.getHeaders();
           final String contentLengthStr = responseHeaders.getFirst(HttpHeaders.CONTENT_LENGTH);
           final long contentLength = contentLengthStr != null ? Long.parseLong(contentLengthStr) : -1;

           final MediaType contentType = responseHeaders.getContentType();
           final String type = contentType != null ? contentType.toString() : "unknown";

           return new RemoteFileMeta(contentLength, type);
       }catch (Exception e){
           throw  new ValidateException("Failed to fetch file size from URL: " + url);
       }
    }


    //检查mimeType是否是视频类型
    public static boolean isVideoMimeType(String mimeType) {
        if (mimeType == null) return false;

        return switch (mimeType.toLowerCase()) {
            case "video/mp4", "video/mpeg", "video/mov", "video/x-flv", "video/avi", "video/webm", "video/wmv",
                 "video/3gpp" -> true;
            default -> false;
        };
    }
    //检查mimeType是否是图片类型
    public static boolean isImageMimeType(String mimeType) {
        if (mimeType == null) return false;

        return switch (mimeType.toLowerCase()) {
            case "image/jpeg", "image/png", "image/heif", "image/heic", "image/webp" -> true;
            default -> false;
        };
    }
    @Getter
    @Setter
    @AllArgsConstructor
    public static class RemoteFileMeta {
        private long contentLength;
        private String contentType;

    }

}
