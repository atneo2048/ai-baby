
package com.ai.baby.sqlagent.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReflectionDecision {

    /**
     * 是否允许进入重试流程
     */
    private boolean shouldRetry;

    /**
     * 反思结论
     */
    private String reason;

    /**
     * 传递给 SQL 生成器的反馈
     */
    private String feedback;
}
