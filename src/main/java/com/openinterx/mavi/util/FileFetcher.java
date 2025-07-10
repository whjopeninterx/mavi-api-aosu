package com.openinterx.mavi.util;

import com.openinterx.mavi.exception.ValidateException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class FileSizeFetcher {

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
    @Getter
    @Setter
    @AllArgsConstructor
    public static class RemoteFileMeta {
        private long contentLength;
        private String contentType;

    }

}
