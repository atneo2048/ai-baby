package com.ai.baby.sqlagent.prompt;

import java.util.List;

import com.ai.baby.sqlagent.domain.SchemaInfo;

import lombok.Data;

@Data
public class PromptContext {

    /**
     * 用户问题
     */
    private String question;


    /**
     * 数据库Schema
     */
    private List<SchemaInfo> schemaList;


    /**
     * 安全规则
     */
    private String securityRule;


    /**
     * 示例SQL
     */
    private String examples;
}
