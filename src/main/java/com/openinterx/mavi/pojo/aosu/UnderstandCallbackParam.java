package com.openinterx.mavi.pojo.aosu;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openinterx.mavi.pojo.common.TokenRes;
import lombok.Getter;
import lombok.Setter;

public class UnderstandCallbackParam {
    @Getter
    @Setter
    public static class CallBackReq{
        @JsonProperty("status")
        private Integer status;
        @JsonProperty("task_id")
        private String taskId;
        @JsonProperty("data")
        private CallBackData data;
        @JsonProperty("token")
        private TokenRes token;


    }
    @Getter
    @Setter
    public static class CallBackData{
        @JsonProperty("text")
        private String text;
    }


}
