package com.ai.baby.sqlagent.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryResult {

    /**
     * 执行的SQL
     */
    private String sql;

    /**
     * 查询结果
     */
    private List<Map<String, Object>> data;

    /**
     * 是否执行成功
     */
    private boolean success;

    /**
     * 执行耗时
     */
    private long costMs;

    /**
     * 错误信息
     */
    private String error;
}