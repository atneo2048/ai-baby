package com.ai.baby.sqlagent.schema.domain;

import lombok.Builder;
import lombok.Data;

/**
 * Schema 业务关键词。
 * 例如：
 * 员工 -> employee
 * 工资 -> employee_salary.salary
 * 部门 -> department
 * 项目 -> project
 */
@Data
@Builder
public class SchemaKeyword {

    /**
     * 用户问题中的业务关键词
     */
    private String keyword;

    /**
     * 关键词对应的目标表
     */
    private String tableName;

    /**
     * 关键词对应的目标字段。
     *
     * 可以为空。
     */
    private String columnName;

    /**
     * 匹配权重
     */
    private double weight;
}