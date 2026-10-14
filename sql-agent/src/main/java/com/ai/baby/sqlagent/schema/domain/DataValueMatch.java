package com.ai.baby.sqlagent.schema.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataValueMatch {

    /**
     * 用户问题中的原始值
     *
     * 例如：
     * 研发部
     * SQL Agent 智能查询系统
     */
    private String value;

    /**
     * 匹配到的表
     */
    private String tableName;

    /**
     * 匹配到的字段
     */
    private String columnName;

    /**
     * 数据库中的实际值
     */
    private String matchedValue;

    /**
     * 匹配得分
     */
    private double score;

    /**
     * 匹配原因
     */
    private String reason;
}