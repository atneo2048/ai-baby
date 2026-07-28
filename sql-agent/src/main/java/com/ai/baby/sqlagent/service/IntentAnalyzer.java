package com.ai.baby.sqlagent.service;

/**
 * 意图分析器
 */
import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.domain.IntentResult;
import com.ai.baby.sqlagent.domain.IntentType;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class IntentAnalyzer {

    private final AIIntentAnalyzer aiAnalyzer;

    public IntentAnalyzer(AIIntentAnalyzer aiAnalyzer) {
        this.aiAnalyzer = aiAnalyzer;
    }

    public IntentResult analyze(String question) {

        IntentResult result = ruleAnalyze(question);

        if (result != null) {
            return result;
        }

        try {
            return aiAnalyzer.analyze(question);
        } catch (JsonProcessingException e) {
            log.error("AI意图分析器解析JSON失败", e);
        }
        return result;
    }

    public IntentResult ruleAnalyze(String question) {

        IntentResult result = new IntentResult();
        result.setQuestion(question);

        String q = question.toLowerCase();

        if (containsDangerous(q)) {
            result.setIntentType(IntentType.DANGEROUS);
            result.setReason("包含危险操作");
            return result;
        }

        if (containsSchema(q)) {
            result.setIntentType(IntentType.SCHEMA_QUERY);
            result.setReason("查询数据库结构");
            return result;
        }

        if (containsSql(q)) {
            result.setIntentType(IntentType.SQL_QUERY);
            result.setReason("数据库查询");
            return result;
        }

        return null;
    }

    private boolean containsSql(String question) {
        return question.contains("查询")
                || question.contains("统计")
                || question.contains("平均")
                || question.contains("工资")
                || question.contains("人数")
                || question.contains("部门")
                || question.contains("订单");

    }

    private boolean containsSchema(String question) {

        return question.contains("有哪些表")
                || question.contains("字段")
                || question.contains("schema")
                || question.contains("数据库结构");

    }

    private boolean containsDangerous(String question) {
        return question.contains("删除")
                || question.contains("drop")
                || question.contains("truncate")
                || question.contains("update")
                || question.contains("修改");
    }
}
