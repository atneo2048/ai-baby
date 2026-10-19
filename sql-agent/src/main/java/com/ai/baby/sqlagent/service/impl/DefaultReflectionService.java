package com.ai.baby.sqlagent.service.impl;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.domain.AgentExecutionContext;
import com.ai.baby.sqlagent.domain.ReflectionDecision;
import com.ai.baby.sqlagent.domain.SqlErrorType;
import com.ai.baby.sqlagent.service.ReflectionService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DefaultReflectionService implements ReflectionService {

    @Override
    public ReflectionDecision reflect(AgentExecutionContext context) {

        if (context == null) {
            throw new IllegalArgumentException("AgentExecutionContext must not be null");
        }

        SqlErrorType errorType = context.getLastErrorType();
        String error = context.getError();

        if (errorType == null) {
            return decision(false, "没有可供分析的 SQL 错误类型", null);
        }

        return switch (errorType) {
            case COLUMN_NOT_FOUND, TABLE_NOT_FOUND, SYNTAX_ERROR ->
                    decision(true, "SQL 结构或语法错误，可以尝试重新生成",
                            "上一次 SQL 执行失败。错误类型：" + errorType
                                    + "；错误详情：" + error
                                    + "。请重新检查表名、列名、关联关系和 SQL 语法，"
                                    + "只使用提供的 Schema 信息生成 SQL。");

            case CONNECTION_ERROR, PERMISSION_ERROR, TIMEOUT, UNKNOWN ->
                    decision(false, "当前错误不适合通过重新生成 SQL 解决", null);
        };
    }

    private ReflectionDecision decision(boolean shouldRetry, String reason, String feedback) {

        log.info("[REFLECTION] shouldRetry={}, reason={}", shouldRetry, reason);

        return ReflectionDecision.builder()
                .shouldRetry(shouldRetry)
                .reason(reason)
                .feedback(feedback)
                .build();
    }
}
