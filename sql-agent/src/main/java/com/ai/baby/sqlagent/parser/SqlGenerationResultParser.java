package com.ai.baby.sqlagent.parser;

import org.springframework.stereotype.Component;

import com.ai.baby.sqlagent.domain.SqlGenerationResult;
import com.ai.baby.sqlagent.util.LlmResponseSanitizer;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SqlGenerationResultParser implements Parser<SqlGenerationResult> {

    private final ObjectMapper objectMapper;

    public SqlGenerationResultParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public SqlGenerationResult parse(String content) {

        try {
            String json = LlmResponseSanitizer.sanitizeToJson(content);
            return objectMapper.readValue(json, SqlGenerationResult.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("SQL生成结果解析失败", e);
        }
    }
}
