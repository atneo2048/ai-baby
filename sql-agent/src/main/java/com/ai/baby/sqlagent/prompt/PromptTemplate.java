package com.ai.baby.sqlagent.prompt;

public class PromptTemplate {

    
    public static final String SQL_AGENT = """
            你是一名专业SQL Agent。
            你的任务：
            根据用户问题生成正确SQL。
            数据库结构：
            %s
            安全规则：
            参考示例：
            要求：
            1. 查询数据库前必须获取Schema
            2. 只能生成SELECT
            3. 不允许修改数据
            4. SQL必须符合数据库语法
            用户问题：
            %s
            """;
}
