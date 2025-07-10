package com.openinterx.mavi.pojo.system;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


public class UnderstandParams {

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UnderstandReq {
        @JsonProperty("video_url")
        private String videoUrl;
        private String videoType; // 视频类型
        @JsonProperty("user_prompt")
        private String userPrompt;
        @JsonProperty("system_prompt")
        private String systemPrompt;
        @JsonProperty("persons")
        private List<Person> persons;
        @JsonProperty("callback")
        private String callback;


    }

    @Getter
    @Setter
    public static class Person{
        @JsonProperty("name")
        private String name;
        @JsonProperty("url")
        private String url;
        private String imgType; // 图片类型
    }

    @Getter
    @Setter
    public static class  UnderstandRes{
        @JsonProperty("task_id")
        private String taskId;
    }


}
