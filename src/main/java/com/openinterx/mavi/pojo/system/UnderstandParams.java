package com.openinterx.mavi.pojo.system;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnderstandReq {
    @JsonProperty("video_url")
    private String videoUrl;
    @JsonProperty("user_prompt")
    private String userPrompt;
    @JsonProperty()
    private String systemPrompt;

}
