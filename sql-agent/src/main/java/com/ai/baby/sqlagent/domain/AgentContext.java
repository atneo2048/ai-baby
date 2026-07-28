package com.ai.baby.sqlagent.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentContext {
    /**
     * 用户原始问题
     */
    private String question;

    /**
     * 意图分析结果
     */
    private IntentResult intent;

    /**
     * 检索到的 Schema
     */
    private List<SchemaInfo> schemas;

    /**
     * 数据库类型
     */
    private String databaseType;

    /**
     * SQL规则
     */
    private List<String> rules;
}
