package com.ai.baby.sqlagent.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.analyzer.IntentAnalyzer;
import com.ai.baby.sqlagent.domain.AgentExecutionContext;
import com.ai.baby.sqlagent.domain.AgentExecutionRecord;
import com.ai.baby.sqlagent.domain.AgentResponse;
import com.ai.baby.sqlagent.domain.AgentStage;
import com.ai.baby.sqlagent.domain.GuardResult;
import com.ai.baby.sqlagent.domain.IntentResult;
import com.ai.baby.sqlagent.domain.QueryResult;
import com.ai.baby.sqlagent.domain.ReflectionDecision;
import com.ai.baby.sqlagent.domain.SqlErrorType;
import com.ai.baby.sqlagent.domain.SqlGenerationResult;
import com.ai.baby.sqlagent.exception.SqlBlockedException;
import com.ai.baby.sqlagent.exception.SqlExecutionException;
import com.ai.baby.sqlagent.guard.SqlGuard;
import com.ai.baby.sqlagent.schema.domain.QueryContext;
import com.ai.baby.sqlagent.schema.service.QueryContextBuilder;
import com.ai.baby.sqlagent.sql.service.SqlGenerator;
import com.ai.baby.sqlagent.tool.DatabaseTool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SQL智能体服务，主编排管线的唯一入口。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SqlAgentService {

    private final IntentAnalyzer intentAnalyzer;
    private final QueryContextBuilder queryContextBuilder;
    private final SqlGenerator sqlGenerator;
    private final SqlGuard sqlGuard;
    private final DatabaseTool databaseTool;
    private final AgentExecutionAuditService agentExecutionAuditService;
    private final ReflectionService reflectionService;
    private final SqlErrorClassifier sqlErrorClassifier;

    private static final int MAX_RETRIES = 2;

    public AgentResponse chat(String question) {

        long start = System.currentTimeMillis();

        AgentExecutionContext context = AgentExecutionContext.builder()
                .executionId(UUID.randomUUID().toString())
                .question(question)
                .stage(AgentStage.INIT)
                .retryCount(0)
                .build();

        try {
            analyzeIntent(context);
            retrieveSchema(context);
            generateSql(context);
            executeWithRetry(context);

            AgentResponse response = buildResponse(context);
            context.setStage(AgentStage.COMPLETED);

            long totalTime = System.currentTimeMillis() - start;
            agentExecutionAuditService.record(buildExecutionRecord(context, totalTime));

            return response;
        } catch (Exception e) {
            log.error("SQL Agent 执行异常", e);
            context.setStage(AgentStage.FAILED);
            context.setError(e.getMessage());

            long totalTime = System.currentTimeMillis() - start;
            agentExecutionAuditService.record(buildExecutionRecord(context, totalTime));

            return buildErrorResponse(context, e);
        }
    }

    private void executeWithRetry(AgentExecutionContext context) {

        while (true) {
            try {
                guardSql(context);
                executeSql(context);
                return;
            } catch (SqlBlockedException e) {
                throw e;
            } catch (SqlExecutionException e) {

                String error = e.getMessage();
                context.setError(error);

                SqlErrorType errorType = sqlErrorClassifier.classify(error);
                context.setLastErrorType(errorType);

                log.warn("[SQL-AGENT][{}] SQL execution failed: type={}, error={}",
                        context.getExecutionId(), errorType, error);

                ReflectionDecision decision = reflectionService.reflect(context);

                log.info("[SQL-AGENT][{}] Reflection: shouldRetry={}, reason={}",
                        context.getExecutionId(), decision.isShouldRetry(), decision.getReason());

                if (!decision.isShouldRetry()) {
                    throw e;
                }

                if (context.getRetryCount() >= MAX_RETRIES) {
                    log.warn("[SQL-AGENT][{}] Retry limit reached: {}",
                            context.getExecutionId(), MAX_RETRIES);
                    throw e;
                }

                context.setRetryCount(context.getRetryCount() + 1);
                regenerateSql(context, decision.getFeedback());
            }
        }
    }

    private void analyzeIntent(AgentExecutionContext context) {

        logStage(context, "analyze intent");
        context.setStage(AgentStage.INTENT);

        IntentResult intent = intentAnalyzer.analyze(context.getQuestion());
        context.setIntent(intent);

        logStage(context, "Intent = " + intent.getIntentType()
                + ", confidence=" + intent.getConfidence());
    }

    private void retrieveSchema(AgentExecutionContext context) {

        context.setStage(AgentStage.SCHEMA);
        logStage(context, "开始Schema检索");

        QueryContext queryContext = queryContextBuilder.build(context.getQuestion());
        context.setQueryContext(queryContext);

        logStage(context, "Schema数量=" + queryContext.getSchemas().size());
    }

    private void generateSql(AgentExecutionContext context) {

        context.setStage(AgentStage.SQL_GENERATION);

        SqlGenerationResult result = sqlGenerator.generate(context.getQueryContext());
        context.setSqlResult(result);

        logStage(context, "生成SQL: " + result.getSql());
    }

    private void regenerateSql(AgentExecutionContext context, String feedback) {

        context.setStage(AgentStage.SQL_GENERATION);
        logStage(context, "重新生成SQL, retryCount=" + context.getRetryCount());

        QueryContext queryContext = context.getQueryContext();
        queryContext.setFeedback(feedback);

        SqlGenerationResult result = sqlGenerator.generate(queryContext);
        context.setSqlResult(result);

        logStage(context, "重试生成SQL: " + result.getSql());
    }

    private void guardSql(AgentExecutionContext context) {

        context.setStage(AgentStage.SQL_GUARD);

        GuardResult result = sqlGuard.check(context.getSqlResult().getSql());
        context.setGuardResult(result);

        logStage(context, "Guard: allowed=" + result.isAllowed()
                + ", risk=" + result.getRisk());

        if (!result.isAllowed()) {
            throw new SqlBlockedException(result.getReason());
        }
    }

    private void executeSql(AgentExecutionContext context) {

        context.setStage(AgentStage.SQL_EXECUTION);

        QueryResult result = databaseTool.executeSql(context.getSqlResult().getSql());
        context.setQueryResult(result);

        int rows = result.getData() == null ? 0 : result.getData().size();
        logStage(context, "SQL执行完成，rows=" + rows + ", cost=" + result.getCostMs() + "ms");

        if (!result.isSuccess()) {
            throw new SqlExecutionException(result.getError());
        }
    }

    private AgentResponse buildResponse(AgentExecutionContext context) {

        context.setStage(AgentStage.RESPONSE);

        SqlGenerationResult sqlResult = context.getSqlResult();
        GuardResult guard = context.getGuardResult();
        QueryResult queryResult = context.getQueryResult();

        return AgentResponse.builder()
                .question(context.getQuestion())
                .intent(context.getIntent())
                .sql(sqlResult.getSql())
                .explanation(sqlResult.getExplanation())
                .risk(guard.getRisk())
                .data(queryResult.getData())
                .success(true)
                .build();
    }

    private AgentResponse buildErrorResponse(AgentExecutionContext context, Exception e) {

        SqlGenerationResult sqlResult = context.getSqlResult();
        GuardResult guard = context.getGuardResult();

        return AgentResponse.builder()
                .question(context.getQuestion())
                .intent(context.getIntent())
                .sql(sqlResult == null ? null : sqlResult.getSql())
                .risk(guard == null ? null : guard.getRisk())
                .success(false)
                .error(e.getMessage())
                .build();
    }

    private void logStage(AgentExecutionContext context, String message) {

        log.info("[SQL-AGENT][{}][{}] {}",
                context.getExecutionId(), context.getStage(), message);
    }

    private AgentExecutionRecord buildExecutionRecord(AgentExecutionContext context, long totalTimeMs) {

        IntentResult intent = context.getIntent();
        GuardResult guard = context.getGuardResult();
        QueryResult query = context.getQueryResult();
        SqlGenerationResult sqlResult = context.getSqlResult();

        return AgentExecutionRecord.builder()
                .executionId(context.getExecutionId())
                .question(context.getQuestion())
                .intent(intent == null || intent.getIntentType() == null
                        ? null : intent.getIntentType().name())
                .confidence(intent == null ? null : intent.getConfidence())
                .sql(sqlResult == null ? null : sqlResult.getSql())
                .risk(guard == null ? null : guard.getRisk())
                .guardAllowed(guard == null ? null : guard.isAllowed())
                .guardReason(guard == null ? null : guard.getReason())
                .rowCount(query == null || query.getData() == null ? 0 : query.getData().size())
                .executionTimeMs(query == null ? null : query.getCostMs())
                .totalTimeMs(totalTimeMs)
                .success(context.getStage() == AgentStage.COMPLETED
                        && query != null && query.isSuccess())
                .stage(context.getStage())
                .error(context.getError())
                .createTime(LocalDateTime.now())
                .build();
    }
}
