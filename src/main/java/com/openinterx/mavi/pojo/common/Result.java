package com.openinterx.mavi.pojo.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Result<T>{
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        final Result<T> resultResponse = new Result<T>();
        resultResponse.setCode(0);
        resultResponse.setMsg("success");
        resultResponse.setData(data);
        return resultResponse;
    }

    public static <T> Result<T> fail(String message) {
        final Result<T> resultResponse = new Result<T>();
        resultResponse.setCode(-1);
        resultResponse.setMsg("failure: "+message);
        return resultResponse;
    }

}
