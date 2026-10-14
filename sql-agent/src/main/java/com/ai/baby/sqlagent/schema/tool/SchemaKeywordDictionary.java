package com.ai.baby.sqlagent.schema.tool;

import com.ai.baby.sqlagent.schema.domain.SchemaKeyword;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Schema 业务关键词字典。
 *
 * V1 版本使用人工维护的确定性映射。
 *
 * 后续可以升级成：
 *
 * V1：
 *   关键词字典
 *
 * V2：
 *   LLM 辅助
 *
 * V3：
 *   Embedding + Vector DB + Reranker
 */
@Component
public class SchemaKeywordDictionary {

    private final List<SchemaKeyword> keywords = List.of(

            // =====================================================
            // 员工
            // =====================================================

            SchemaKeyword.builder()
                    .keyword("员工")
                    .tableName("employee")
                    .weight(10)
                    .build(),

            SchemaKeyword.builder()
                    .keyword("职员")
                    .tableName("employee")
                    .weight(10)
                    .build(),

            SchemaKeyword.builder()
                    .keyword("人员")
                    .tableName("employee")
                    .weight(10)
                    .build(),

            SchemaKeyword.builder()
                    .keyword("雇员")
                    .tableName("employee")
                    .weight(10)
                    .build(),

            // =====================================================
            // 工资
            // =====================================================

            SchemaKeyword.builder()
                    .keyword("工资")
                    .tableName("employee_salary")
                    .columnName("salary")
                    .weight(10)
                    .build(),

            SchemaKeyword.builder()
                    .keyword("薪资")
                    .tableName("employee_salary")
                    .columnName("salary")
                    .weight(10)
                    .build(),

            SchemaKeyword.builder()
                    .keyword("薪水")
                    .tableName("employee_salary")
                    .columnName("salary")
                    .weight(10)
                    .build(),

            SchemaKeyword.builder()
                    .keyword("收入")
                    .tableName("employee_salary")
                    .columnName("salary")
                    .weight(8)
                    .build(),

            // =====================================================
            // 部门
            // =====================================================

            SchemaKeyword.builder()
                    .keyword("部门")
                    .tableName("department")
                    .weight(10)
                    .build(),

            SchemaKeyword.builder()
                    .keyword("团队")
                    .tableName("department")
                    .weight(8)
                    .build(),

            SchemaKeyword.builder()
                    .keyword("组织")
                    .tableName("department")
                    .weight(8)
                    .build(),

            // =====================================================
            // 项目
            // =====================================================

            SchemaKeyword.builder()
                    .keyword("项目")
                    .tableName("project")
                    .weight(10)
                    .build(),

            SchemaKeyword.builder()
                    .keyword("工程")
                    .tableName("project")
                    .weight(8)
                    .build()
    );

    /**
     * 根据用户问题匹配业务关键词。
     */
    public List<SchemaKeyword> match(String question) {

        if (question == null || question.isBlank()) {
            return List.of();
        }

        return keywords.stream()
                .filter(keyword ->
                        question.contains(keyword.getKeyword()))
                .toList();
    }

    /**
     * 返回全部关键词。
     *
     * 方便后续测试或调试。
     */
    public List<SchemaKeyword> getAll() {
        return keywords;
    }
}