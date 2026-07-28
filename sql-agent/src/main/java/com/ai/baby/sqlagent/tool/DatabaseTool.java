package com.ai.baby.sqlagent.tool;

import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.ai.baby.sqlagent.domain.SqlAuditRecord;
import com.ai.baby.sqlagent.security.SqlAuditService;
import com.ai.baby.sqlagent.security.SqlGuard;

import dev.langchain4j.agent.tool.Tool;

@Component
public class DatabaseTool {

    private final JdbcTemplate jdbcTemplate;

    private final SqlGuard sqlGuard;

    private final SqlAuditService auditService;

    public DatabaseTool(JdbcTemplate jdbcTemplate, SqlGuard sqlGuard, SqlAuditService auditService) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlGuard = sqlGuard;
        this.auditService = auditService;
    }

    @Tool("""
            执行SQL查询。
            只能执行SELECT语句。
            """)
    public List<Map<String, Object>> executeSql(
            String sql) {

        long start = System.currentTimeMillis();

        try {
            // 1.SQL安全检查
            String safeSql = sqlGuard.checkAndRewrite(sql);

            // 2.执行SQL
            List<Map<String, Object>> result = jdbcTemplate
                    .queryForList(safeSql);

            // 3.记录成功日志
            SqlAuditRecord record = new SqlAuditRecord();
            record.setSql(sql);
            record.setSuccess(true);
            record.setCost(
                    System.currentTimeMillis()
                            -
                            start);
            auditService.record(record);
            return result;
        } catch (Exception e) {
            SqlAuditRecord record = new SqlAuditRecord();
            record.setSql(sql);
            record.setSuccess(false);
            record.setError(
                    e.getMessage());
            auditService.record(record);
            throw e;
        }
    }
}
