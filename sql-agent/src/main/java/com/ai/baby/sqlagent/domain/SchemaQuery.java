package com.ai.baby.sqlagent.domain;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SchemaQuery {

    /**
     * 用户问题
     */
    private String question;

    /**
     * 可能涉及的业务关键词
     */
    private List<String> keywords;

    /**
     * 可能涉及的表
     */
    private List<String> tables;

    /**
     * 可能涉及的字段
     */
    private List<String> columns;
}