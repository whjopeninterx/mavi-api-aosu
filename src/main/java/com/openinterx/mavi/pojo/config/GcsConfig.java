package com.openinterx.mavi.pojo.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GcsConfig {
    private String projectId;
    private String scoped;
    private String authJsonPath;
    private String bucket;

}
