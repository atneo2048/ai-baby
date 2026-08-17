package com.ai.baby.sqlagent.domain;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class RetryPolicy {

    /**
     * 最大重试次数
     */
    private int maxRetries;

    /**
     * 当前重试次数
     */
    private int retryCount;
}