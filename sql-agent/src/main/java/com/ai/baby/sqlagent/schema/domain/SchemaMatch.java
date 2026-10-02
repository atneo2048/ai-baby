package com.ai.baby.sqlagent.schema.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SchemaMatch {

    /**
     * 表
     */
    private SchemaInfo schema;

    /**
     * 匹配得分
     */
    private double score;

    /**
     * 匹配原因
     */
    private List<String> reasons;
}