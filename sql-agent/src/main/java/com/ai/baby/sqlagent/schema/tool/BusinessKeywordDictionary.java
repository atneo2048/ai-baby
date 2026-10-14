package com.ai.baby.sqlagent.schema.tool;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BusinessKeywordDictionary {

    private final Map<String, Set<String>> dictionary =
            new ConcurrentHashMap<>();

    public BusinessKeywordDictionary() {

        register(
                "salary",
                "employee_salary",
                "工资",
                "薪资",
                "薪水",
                "收入",
                "月薪",
                "年薪"
        );

        register(
                "employee",
                "employee",
                "员工",
                "职员",
                "人员",
                "雇员"
        );

        register(
                "department",
                "department",
                "部门",
                "团队",
                "组织"
        );

        register(
                "project",
                "project",
                "项目",
                "工程"
        );
    }

    private void register(
            String schemaKeyword,
            String... businessKeywords) {

        dictionary.put(
                schemaKeyword,
                new HashSet<>(
                        Arrays.asList(businessKeywords)
                )
        );
    }

    /**
     * 根据业务词找到对应的 Schema 关键词
     */
    public Set<String> findSchemaKeywords(
            String question) {

        Set<String> result =
                new HashSet<>();

        if (question == null || question.isBlank()) {
            return result;
        }

        for (Map.Entry<String, Set<String>> entry :
                dictionary.entrySet()) {

            for (String keyword :
                    entry.getValue()) {

                if (question.contains(keyword)) {

                    result.add(
                            entry.getKey()
                    );

                    break;
                }
            }
        }

        return result;
    }
}