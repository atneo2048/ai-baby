package com.ai.baby.sqlagent.sql.service.impl;

import com.ai.baby.sqlagent.domain.GuardResult;
import com.ai.baby.sqlagent.domain.QueryResult;
import com.ai.baby.sqlagent.guard.SqlGuard;
import com.ai.baby.sqlagent.schema.domain.QueryContext;
import com.ai.baby.sqlagent.sql.service.SqlExtractor;
import com.ai.baby.sqlagent.sql.service.SqlGenerator;
import com.ai.baby.sqlagent.sql.service.SqlQueryService;
import com.ai.baby.sqlagent.tool.DatabaseTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultSqlQueryService implements SqlQueryService {

    private final SqlGenerator sqlGenerator;

    private final SqlExtractor sqlExtractor;

    private final SqlGuard sqlGuard;

    private final DatabaseTool databaseTool;

    @Override
    public QueryResult execute(QueryContext context) {

        if (context == null) {
            throw new IllegalArgumentException(
                    "QueryContext must not be null");
        }

        log.info(
                "========== SQL Query Pipeline ==========");

        // =====================================================
        // 1. LLM 生成 SQL
        // =====================================================

        String rawResponse =
                sqlGenerator.generate(context);

        log.info(
                "Step 1 - Raw LLM Response:");

        log.info(
                "{}",
                rawResponse);

        // =====================================================
        // 2. 提取纯 SQL
        // =====================================================

        String sql =
                sqlExtractor.extract(rawResponse);

        log.info(
                "Step 2 - Extracted SQL:");

        log.info(
                "{}",
                sql);

        // =====================================================
        // 3. SQL 安全检查
        // =====================================================

        GuardResult guardResult =
                sqlGuard.check(sql);

        log.info(
                "Step 3 - SQL Guard Result: allowed={}, risk={}, reason={}",
                guardResult.isAllowed(),
                guardResult.getRisk(),
                guardResult.getReason());

        // =====================================================
        // 4. SQL 被禁止
        // =====================================================

        if (!guardResult.isAllowed()) {

            log.warn(
                    "SQL execution blocked: {}",
                    guardResult.getReason());

            return QueryResult.builder()
                    .sql(sql)
                    .success(false)
                    .error(
                            "SQL安全检查未通过："
                                    + guardResult.getReason())
                    .build();
        }

        log.info(
                "Step 3 - SQL Guard Passed");

        // =====================================================
        // 5. 执行 SQL
        // =====================================================

        QueryResult queryResult =
                databaseTool.executeSql(sql);

        log.info(
                "Step 4 - SQL Executed, success={}, costMs={}",
                queryResult.isSuccess(),
                queryResult.getCostMs());

        // =====================================================
        // 6. Pipeline结束
        // =====================================================

        log.info(
                "========== SQL Query Pipeline Completed ==========");

        return queryResult;
    }
}