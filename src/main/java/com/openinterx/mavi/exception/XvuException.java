package com.openInterX.common.exception;

/**
 * @author whj
 * @description mava 全局错误无法处理的问题使用这个异常建议后期发送短信通知
 * @createtime: 20240905
 */
public class XvuException extends RuntimeException {


    public XvuException() {
        super();
    }

    public XvuException(String message) {
        super(message);
    }

    public XvuException(String message, Throwable cause) {
        super(message, cause);
    }

    public XvuException(Throwable cause) {
        super(cause);
    }
}
