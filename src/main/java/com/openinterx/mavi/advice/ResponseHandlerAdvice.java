package com.openinterx.mavi.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openinterx.mavi.pojo.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author whj
 * @description 返回统一处理
 * @createtime: 20240905
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ResponseHandlerAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> clazz) {
        log.info("返回信息统一处理supports:");
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> clazz, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        log.info("返回信息统一处理beforeBodyWrite:");
        // 检查 MediaType 是否为 JSON 类型
        if (!MediaType.APPLICATION_JSON.equalsTypeAndSubtype(mediaType)) {
            return body; // 非 JSON 响应直接返回
        }
        if (body instanceof Result<?>) {
            return body;
        }
        return Result.success(body);
    }
}
