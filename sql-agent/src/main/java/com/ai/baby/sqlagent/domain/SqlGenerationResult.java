package com.ai.baby.sqlagent.domain;

import com.ai.baby.sqlagent.enums.RiskLevel;

import lombok.Data;

@Data
public class SqlGenerationResult {

    /**
     * 生成的SQL
     */
    private String sql;

    /**
     * SQL生成原因
     */
    private String explanation;

    /**
     * 风险等级
     */
    private RiskLevel risk;
}
