package com.ai.baby.sqlagent.parser;

import org.springframework.stereotype.Component;

import com.ai.baby.sqlagent.domain.SqlGenerationResult;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SqlGenerationResultParser implements Parser<SqlGenerationResult> {

    private final ObjectMapper objectMapper;

    public SqlGenerationResultParser(
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public SqlGenerationResult parse(
            String content) {

        try {

            String json =
                    extractJson(content);

            return objectMapper.readValue(
                    json,
                    SqlGenerationResult.class
            );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "SQL生成结果解析失败",
                    e
            );

        }
    }

    private String extractJson(String content) {

        if (content == null ||
                content.isBlank()) {

            throw new IllegalArgumentException(
                    "LLM返回为空"
            );
        }

        content = content.trim();

        // 去掉Markdown代码块
        content = content
                .replace("```json", "")
                .replace("```", "")
                .trim();

        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");

        if (start < 0 || end < start) {

            throw new IllegalArgumentException(
                    "LLM返回中不存在JSON"
            );
        }

        return content.substring(
                start,
                end + 1
        );
    }
}
