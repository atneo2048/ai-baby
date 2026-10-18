package com.ai.baby.sqlagent.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.ai.baby.sqlagent.domain.*;
import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.analyzer.IntentAnalyzer;
import com.ai.baby.sqlagent.schema.domain.SchemaInfo;
import com.ai.baby.sqlagent.exception.SqlBlockedException;
import com.ai.baby.sqlagent.exception.SqlExecutionException;
import com.ai.baby.sqlagent.generator.SqlGenerator;
import com.ai.baby.sqlagent.guard.SqlGuard;
import com.ai.baby.sqlagent.schema.service.SchemaRetriever;
import com.ai.baby.sqlagent.tool.DatabaseTool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SQL智能体服务
 */
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

            buildContext(context);

            // 第一次生成
            generateSql(context);

            // 自动执行 + 重试
            executeWithRetry(context);

            AgentResponse response = buildResponse(context);

            context.setStage(AgentStage.COMPLETED);

            long totalTime = System.currentTimeMillis() - start;

            AgentExecutionRecord record = buildExecutionRecord(context, totalTime);

            agentExecutionAuditService.record(record);

            return response;

        } catch (Exception e) {

            log.error("调用大模型异常", e);
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

                // 1. 保存本次 SQL 执行错误
                String error = e.getMessage();
                context.setError(error);

                // 2. 对本次错误分类
                SqlErrorType errorType =
                        sqlErrorClassifier.classify(error);

                context.setLastErrorType(errorType);

                log.warn(
                        "[SQL-AGENT][{}] SQL execution failed: type={}, error={}",
                        context.getExecutionId(),
                        errorType,
                        error
                );

                // 3. Reflection 决定是否重试
                ReflectionDecision decision =
                        reflectionService.reflect(context);

                log.info(
                        "[SQL-AGENT][{}] Reflection: shouldRetry={}, reason={}",
                        context.getExecutionId(),
                        decision.isShouldRetry(),
                        decision.getReason()
                );

                // 4. Reflection 不建议重试，直接终止
                if (!decision.isShouldRetry()) {
                    throw e;
                }

                // 5. 达到重试上限，终止
                if (context.getRetryCount() >= MAX_RETRIES) {
                    log.warn(
                            "[SQL-AGENT][{}] Retry limit reached: {}",
                            context.getExecutionId(),
                            MAX_RETRIES
                    );
                    throw e;
                }

                // 6. 增加重试次数并重新生成 SQL
                context.setRetryCount(context.getRetryCount() + 1);

                regenerateSql(context, decision.getFeedback());
            }
        }
    }

    private void regenerateSql(
            AgentExecutionContext context,
            String feedback) {

        context.setQueryResult(null);
        context.setGuardResult(null);

        context.setStage(AgentStage.SCHEMA);

        List<SchemaInfo> schemas =
                schemaRetriever.retrieve(context.getQuestion());

        context.setSchemas(schemas);
        context.getAgentContext().setSchemas(schemas);

        context.setStage(AgentStage.SQL_GENERATION);

        SqlGenerationResult result = sqlGenerator.regenerate(
                context.getAgentContext(),
                context.getSql(),
                feedback
        );

        if (result == null
                || result.getSql() == null
                || result.getSql().isBlank()) {
            throw new IllegalStateException(
                    "Reflection 重新生成 SQL 失败：生成结果为空"
            );
        }

        context.setSqlResult(result);
        context.setSql(result.getSql());

        log.info(
                "[SQL-AGENT][{}] regenerated SQL: {}",
                context.getExecutionId(),
                result.getSql()
        );
    }

    /**
     * 分析用户意图
     */
    private void analyzeIntent(AgentExecutionContext context) {

        logStage(context, "analyze intent");

        context.setStage(AgentStage.INTENT);

        IntentResult intent = intentAnalyzer.analyze(context.getQuestion());

        context.setIntent(intent);

        logStage(context, "Intent = " + intent.getIntentType() + ", confidence=" + intent.getConfidence());
    }

    /**
     * 检索数据库模式
     */
    private void retrieveSchema(AgentExecutionContext context) {

        context.setStage(AgentStage.SCHEMA);

        logStage(context, "开始Schema检索");

        List<SchemaInfo> schemas = schemaRetriever.retrieve(context.getQuestion());

        context.setSchemas(schemas);

        logStage(context, "Schema数量=" + schemas.size());
    }

    private void buildContext(AgentExecutionContext context) {

        context.setStage(AgentStage.CONTEXT);

        AgentContext agentContext = AgentContext.builder()
                .question(
                        context.getQuestion())
                .intent(
                        context.getIntent())
                .schemas(
                        context.getSchemas())
                .databaseType(
                        "Mysql")
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
                .executionId(context.getExecutionId())
                .question(context.getQuestion())

                // 暂时保留原有取值，下一步核对 IntentResult 后再修正
                .intent(
                        intent == null || intent.getIntentType() == null
                                ? null
                                : intent.getIntentType().name()
                )
                .confidence(
                        intent == null
                                ? null
                                : intent.getConfidence()
                )

                .sql(context.getSql())

                .risk(
                        guard == null
                                ? null
                                : guard.getRisk()
                )
                .guardAllowed(
                        guard == null
                                ? null
                                : guard.isAllowed()
                )
                .guardReason(
                        guard == null
                                ? null
                                : guard.getReason()
                )

                // 返回结果的行数，不代表数据库扫描行数
                .rowCount(
                        query == null || query.getData() == null
                                ? 0
                                : query.getData().size()
                )
                .executionTimeMs(
                        query == null
                                ? null
                                : query.getCostMs()
                )
                .totalTimeMs(totalTimeMs)

                .success(
                        context.getStage() == AgentStage.COMPLETED
                                && query != null
                                && query.isSuccess()
                )
                .stage(context.getStage())
                .error(context.getError())
                .createTime(LocalDateTime.now())
                .build();
    }
}