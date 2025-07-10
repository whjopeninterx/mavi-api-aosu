package com.openinterx.mavi.advice;

import com.openinterx.mavi.exception.ValidateException;
import com.openinterx.mavi.pojo.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandlerAdvice {

    @ResponseBody
    @ExceptionHandler
    public Result<Void> validataException(ValidateException exception) {
        log.info("常规参数校验异常：{}", exception.getMessage());
        return Result.fail(exception.getMessage());
    }
}
