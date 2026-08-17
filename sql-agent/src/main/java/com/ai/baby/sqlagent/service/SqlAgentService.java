package com.ai.baby.sqlagent.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.analyzer.IntentAnalyzer;
import com.ai.baby.sqlagent.domain.AgentContext;
import com.ai.baby.sqlagent.domain.AgentExecutionContext;
import com.ai.baby.sqlagent.domain.AgentExecutionRecord;
import com.ai.baby.sqlagent.domain.AgentResponse;
import com.ai.baby.sqlagent.domain.AgentStage;
import com.ai.baby.sqlagent.domain.GuardResult;
import com.ai.baby.sqlagent.domain.IntentResult;
import com.ai.baby.sqlagent.domain.QueryResult;
import com.ai.baby.sqlagent.domain.SchemaInfo;
import com.ai.baby.sqlagent.domain.SqlErrorType;
import com.ai.baby.sqlagent.domain.SqlGenerationResult;
import com.ai.baby.sqlagent.exception.SqlBlockedException;
import com.ai.baby.sqlagent.exception.SqlExecutionException;
import com.ai.baby.sqlagent.generator.SqlGenerator;
import com.ai.baby.sqlagent.guard.SqlGuard;
import com.ai.baby.sqlagent.schema.SchemaRetriever;
import com.ai.baby.sqlagent.tool.DatabaseTool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqlAgentService {

        private final IntentAnalyzer intentAnalyzer;

        private final SchemaRetriever schemaRetriever;

        private final SqlGenerator sqlGenerator;

        private final SqlGuard sqlGuard;

        private final DatabaseTool databaseTool;

        private final AgentExecutionAuditService agentExecutionAuditService;

        private final SqlErrorClassifier sqlErrorClassifier;

        private static final int MAX_RETRIES = 2;

        public AgentResponse chat(
                        String question) {

                long start = System.currentTimeMillis();

                AgentExecutionContext context = AgentExecutionContext.builder()
                                .executionId(
                                                UUID.randomUUID()
                                                                .toString())
                                .question(question)
                                .stage(AgentStage.INIT)
                                .retryCount(0)
                                .build();

                try {

                        analyzeIntent(context);

                        retrieveSchema(context);

                        buildContext(context);

                        // 第一次生成
                        generateSql(context);

                        // 自动执行 + 重试
                        executeWithRetry(context);

                        context.setStage(
                                        AgentStage.COMPLETED);

                        long totalTime = System.currentTimeMillis()
                                        - start;

                        AgentExecutionRecord record = buildExecutionRecord(
                                        context,
                                        totalTime);

                        agentExecutionAuditService.record(record);

                        return buildResponse(context);

                } catch (Exception e) {

                        context.setStage(
                                        AgentStage.FAILED);

                        context.setError(
                                        e.getMessage());

                        long totalTime = System.currentTimeMillis()
                                        - start;

                        AgentExecutionRecord record = buildExecutionRecord(
                                        context,
                                        totalTime);

                        agentExecutionAuditService.record(record);

                        return buildErrorResponse(
                                        context,
                                        e);
                }
        }

        private void executeWithRetry(
                        AgentExecutionContext context) {

                while (true) {

                        try {

                                // ====================
                                // 1. Guard
                                // ====================

                                guardSql(context);

                                // ====================
                                // 2. Execute
                                // ====================

                                executeSql(context);

                                // 成功
                                return;

                        } catch (SqlBlockedException e) {

                                // Guard拦截
                                throw e;

                        } catch (SqlExecutionException e) {

                                context.setError(
                                                e.getMessage());

                                SqlErrorType errorType = sqlErrorClassifier.classify(
                                                e.getMessage());

                                context.setLastErrorType(
                                                errorType);

                                log.warn(
                                                "[SQL-AGENT][{}] SQL execution failed, type={}, retry={}",
                                                context.getExecutionId(),
                                                errorType,
                                                context.getRetryCount());

                                // ====================
                                // 是否可以重试
                                // ====================

                                if (!canRetry(errorType)) {

                                        throw e;
                                }

                                // ====================
                                // 是否超过最大次数
                                // ====================

                                if (context.getRetryCount() >= MAX_RETRIES) {

                                        throw e;
                                }

                                // ====================
                                // Retry
                                // ====================

                                context.setRetryCount(
                                                context.getRetryCount() + 1);

                                regenerateSql(context);
                        }
                }
        }

        private void regenerateSql(
                        AgentExecutionContext context) {

                context.setStage(
                                AgentStage.SCHEMA);

                log.info(
                                "[SQL-AGENT][{}] refreshing schema...",
                                context.getExecutionId());

                List<SchemaInfo> schemas = schemaRetriever.refresh(
                                context.getQuestion(),
                                context.getSql(),
                                context.getError());

                context.setSchemas(
                                schemas);

                /*
                 * 更新AgentContext
                 */
                context.getAgentContext()
                                .setSchemas(
                                                schemas);

                log.info(
                                "[SQL-AGENT][{}] schema refreshed, count={}",
                                context.getExecutionId(),
                                schemas.size());

                // ==========================
                // 重新生成SQL
                // ==========================

                context.setStage(
                                AgentStage.SQL_GENERATION);

                SqlGenerationResult result = sqlGenerator.regenerate(
                                context.getAgentContext(),
                                context.getSql(),
                                context.getError());

                context.setSqlResult(
                                result);

                context.setSql(
                                result.getSql());

                log.info(
                                "[SQL-AGENT][{}] regenerated SQL: {}",
                                context.getExecutionId(),
                                result.getSql());
        }

        private void analyzeIntent(
                        AgentExecutionContext context) {

                logStage(
                                context,
                                "analyze intent");

                context.setStage(
                                AgentStage.INTENT);

                IntentResult intent = intentAnalyzer.analyze(
                                context.getQuestion());

                context.setIntent(intent);

                logStage(
                                context,
                                "Intent = "
                                                + intent.getIntentType()
                                                + ", confidence="
                                                + intent.getConfidence());
        }

        private void retrieveSchema(
                        AgentExecutionContext context) {

                context.setStage(
                                AgentStage.SCHEMA);

                logStage(
                                context,
                                "开始Schema检索");

                List<SchemaInfo> schemas = schemaRetriever.retrieve(
                                context.getQuestion());

                context.setSchemas(schemas);

                logStage(
                                context,
                                "Schema数量="
                                                + schemas.size());
        }

        private void buildContext(
                        AgentExecutionContext context) {

                context.setStage(
                                AgentStage.CONTEXT);

                AgentContext agentContext = AgentContext.builder()
                                .question(
                                                context.getQuestion())
                                .intent(
                                                context.getIntent())
                                .schemas(
                                                context.getSchemas())
                                .databaseType(
                                                "OceanBase")
                                .rules(List.of(
                                                "只允许SELECT",
                                                "禁止INSERT",
                                                "禁止UPDATE",
                                                "禁止DELETE",
                                                "禁止DROP",
                                                "禁止TRUNCATE"))
                                .build();

                context.setAgentContext(
                                agentContext);
        }

        private void generateSql(
                        AgentExecutionContext context) {

                context.setStage(
                                AgentStage.SQL_GENERATION);

                SqlGenerationResult result = sqlGenerator.generate(
                                context.getAgentContext());

                context.setSqlResult(result);

                context.setSql(
                                result.getSql());

                logStage(
                                context,
                                "生成SQL: "
                                                + result.getSql());
        }

        private void guardSql(
                        AgentExecutionContext context) {

                context.setStage(
                                AgentStage.SQL_GUARD);

                GuardResult result = sqlGuard.check(
                                context.getSql());

                context.setGuardResult(result);

                logStage(
                                context,
                                "Guard: allowed="
                                                + result.isAllowed()
                                                + ", risk="
                                                + result.getRisk());

                if (!result.isAllowed()) {

                        throw new SqlBlockedException(
                                        result.getReason());
                }
        }

        private void executeSql(
                        AgentExecutionContext context) {

                context.setStage(
                                AgentStage.SQL_EXECUTION);

                QueryResult result = databaseTool.executeSql(
                                context.getSql());

                context.setQueryResult(result);

                logStage(
                                context,
                                "SQL执行完成，rows="
                                                + (result.getData() == null
                                                                ? 0
                                                                : result.getData().size())
                                                + ", cost="
                                                + result.getCostMs()
                                                + "ms");

                if (!result.isSuccess()) {

                        throw new SqlExecutionException(
                                        result.getError());
                }
        }

        private AgentResponse buildResponse(
                        AgentExecutionContext context) {

                context.setStage(
                                AgentStage.RESPONSE);

                QueryResult queryResult = context.getQueryResult();

                return AgentResponse.builder()
                                .question(
                                                context.getQuestion())
                                .intent(
                                                context.getIntent())
                                .sql(
                                                context.getSql())
                                .explanation(
                                                context.getSqlResult()
                                                                .getExplanation())
                                .risk(
                                                context.getGuardResult()
                                                                .getRisk())
                                .data(
                                                queryResult.getData())
                                .success(true)
                                .build();
        }

        private AgentResponse buildErrorResponse(
                        AgentExecutionContext context,
                        Exception e) {

                return AgentResponse.builder()
                                .question(
                                                context.getQuestion())
                                .intent(
                                                context.getIntent())
                                .sql(
                                                context.getSql())
                                .risk(
                                                context.getGuardResult() == null
                                                                ? null
                                                                : context.getGuardResult()
                                                                                .getRisk())
                                .success(false)
                                .error(
                                                e.getMessage())
                                .build();
        }

        private void logStage(
                        AgentExecutionContext context,
                        String message) {

                log.info(
                                "[SQL-AGENT][{}][{}] {}",
                                context.getExecutionId(),
                                context.getStage(),
                                message);
        }

        private AgentExecutionRecord buildExecutionRecord(
                        AgentExecutionContext context,
                        long totalTimeMs) {

                IntentResult intent = context.getIntent();

                GuardResult guard = context.getGuardResult();

                QueryResult query = context.getQueryResult();

                return AgentExecutionRecord.builder()
                                .executionId(
                                                context.getExecutionId())
                                .question(
                                                context.getQuestion())
                                .intent(
                                                intent == null
                                                                ? null
                                                                : intent.getReason())
                                .confidence(
                                                intent == null
                                                                ? null
                                                                : intent.getConfidence())
                                .sql(
                                                context.getSql())
                                .risk(
                                                guard == null
                                                                ? null
                                                                : guard.getRisk())
                                .guardAllowed(
                                                guard == null
                                                                ? null
                                                                : guard.isAllowed())
                                .guardReason(
                                                guard == null
                                                                ? null
                                                                : guard.getReason())
                                .rowCount(
                                                query == null
                                                                || query.getData() == null
                                                                                ? 0
                                                                                : query.getData().size())
                                .executionTimeMs(
                                                query == null
                                                                ? null
                                                                : query.getCostMs())
                                .totalTimeMs(
                                                totalTimeMs)
                                .success(
                                                query != null
                                                                && query.isSuccess())
                                .stage(
                                                context.getStage())
                                .createTime(
                                                LocalDateTime.now())
                                .build();
        }

        private boolean canRetry(
                        SqlErrorType errorType) {

                return switch (errorType) {

                        case COLUMN_NOT_FOUND,
                                        TABLE_NOT_FOUND,
                                        SYNTAX_ERROR ->
                                true;

                        case CONNECTION_ERROR,
                                        PERMISSION_ERROR,
                                        TIMEOUT,
                                        UNKNOWN ->
                                false;
                };
        }
}