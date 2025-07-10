package com.openinterx.mavi.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@TableName("task_queue")
public class TaskQueue {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("task_id")
    private Long taskId;
    @TableField("param")
    private String param;
    @TableField("callback_url")
    private String callbackUrl;

    @TableField("payload")
    private String payload;

    @TableField("status")
    private String status;
    @TableField("processing")
    private Boolean processing;

    @TableField("retry_count")
    private Integer retryCount;
    @TableField("max_retry")
    private Integer maxRetry;
    @TableField("last_error")
    private String lastError;
    @TableField("updated_at")
    private Long updatedAt;


}