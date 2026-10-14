package com.ai.baby.sqlagent.sql.service.impl;

import com.ai.baby.sqlagent.sql.service.SqlExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DefaultSqlExtractor implements SqlExtractor {

    @Override
    public String extract(String response) {

        if (response == null || response.isBlank()) {
            throw new IllegalArgumentException("LLM SQL response must not be blank");
        }

        String sql = response.trim();

        log.info("========== SQL Extraction ==========");
        log.info("Raw LLM Response:\n{}", sql);

        // 1. 删除 <think>...</think>
        sql = removeThinkBlock(sql);

        // 2. 删除 Markdown SQL 代码块
        sql = sql.replace("```sql", "")
                .replace("```SQL", "")
                .replace("```", "");

        sql = sql.trim();

        if (sql.isBlank()) {
            throw new IllegalStateException("No SQL found in LLM response");
        }

        log.info("Extracted SQL:\n{}", sql);

        return sql;
    }

    private String removeThinkBlock(String text) {

        int start = text.indexOf("<think>");
        int end = text.indexOf("</think>");

        if (start >= 0 && end >= 0 && end > start) {
            return text.substring(0, start)
                    + text.substring(end + "</think>".length());
        }

        return text;
    }
}