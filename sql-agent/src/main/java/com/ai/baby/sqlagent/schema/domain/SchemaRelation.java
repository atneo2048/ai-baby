package com.ai.baby.sqlagent.schema.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SchemaRelation {

    /**
     * 源表
     */
    private String fromTable;

    /**
     * 源字段
     */
    private String fromColumn;

    /**
     * 目标表
     */
    private String toTable;

    /**
     * 目标字段
     */
    private String toColumn;
}