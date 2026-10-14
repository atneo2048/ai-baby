package com.ai.baby.sqlagent.sql.service;

public interface SqlExtractor {


    /**
     * 从 LLM 原始响应中提取纯 SQL
     *
     * @param response LLM 原始响应
     * @return 纯 SQL
     */
    String extract(String response);

}
