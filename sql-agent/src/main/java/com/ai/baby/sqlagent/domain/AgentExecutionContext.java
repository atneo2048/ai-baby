package com.ai.baby.sqlagent.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
     * Schema
     */
    private List<SchemaInfo> schemas;

    /**
     * Agent上下文
     */
    private AgentContext agentContext;

    /**
     * 生成的SQL
     */
    private String sql;

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