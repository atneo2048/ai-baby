package com.ai.baby.sqlagent.domain;

import com.ai.baby.sqlagent.enums.RiskLevel;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentResponse {

    /**
     * 用户原始问题
     */
    private String question;

    /**
     * 用户意图
     */
    private IntentResult intent;

    /**
     * 执行的SQL
     */
    private String sql;

    /**
     * SQL解释
     */
    private String explanation;

    /**
     * SQL风险等级
     */
    private RiskLevel risk;

    /**
     * SQL执行结果
     */
    private Object data;

    /**
     * 是否执行成功
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String error;
}