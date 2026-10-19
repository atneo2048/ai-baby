package com.ai.baby.sqlagent.domain;

import com.ai.baby.sqlagent.schema.domain.QueryContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentExecutionContext {

    /**
     * 一次Agent请求唯一ID
     */
    private String executionId;

    /**
     * 用户原始问题
     */
    private String question;

    /**
     * Intent
     */
    private IntentResult intent;

    /**
     * Agent上下文（含 Schema、实体、数据值、关系）
     */
    private QueryContext queryContext;

    /**
     * SQL生成结果
     */
    private SqlGenerationResult sqlResult;

    /**
     * SQL安全检查结果
     */
    private GuardResult guardResult;

    /**
     * SQL执行结果
     */
    private QueryResult queryResult;

    /**
     * 当前执行阶段
     */
    private AgentStage stage;

    /**
     * 本次执行错误
     */
    private String error;

    /**
     * 重试次数
     */
    private int retryCount;

    /**
     * 上一次执行错误类型
     */
    private SqlErrorType lastErrorType;
}
