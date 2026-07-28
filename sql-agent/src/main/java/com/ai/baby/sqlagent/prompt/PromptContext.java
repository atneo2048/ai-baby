package com.ai.baby.sqlagent.prompt;

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
    private String schema;


    /**
     * 安全规则
     */
    private String securityRule;


    /**
     * 示例SQL
     */
    private String examples;
}
