package com.ai.baby.sqlagent.test;

import com.ai.baby.sqlagent.domain.QueryResult;
import com.ai.baby.sqlagent.schema.domain.QueryContext;
import com.ai.baby.sqlagent.schema.service.QueryContextBuilder;
import com.ai.baby.sqlagent.sql.service.SqlQueryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class SqlAgentPipelineTest {

    @Autowired
    private QueryContextBuilder queryContextBuilder;

    @Autowired
    private SqlQueryService sqlQueryService;

    @Test
    void testFullSqlAgentPipeline() {

        // =====================================================
        // 1. 用户问题
        // =====================================================

        String question = "查询研发部的平均工资";

        log.info("");
        log.info("==================================================");
        log.info("        SQL Agent Pipeline Test");
        log.info("==================================================");
        log.info("User Question: {}", question);


        // =====================================================
        // 2. 构建 QueryContext
        // =====================================================

        log.info("");
        log.info("========== Step 1: Build QueryContext ==========");

        QueryContext context =
                queryContextBuilder.build(question);

        log.info("QueryContext:");
        log.info("Question: {}", context.getQuestion());
        log.info("Entities: {}", context.getEntities());
        log.info("Schemas: {}", context.getSchemas());
        log.info("DataValues: {}", context.getDataValues());
        log.info("Relations: {}", context.getRelations());


        // =====================================================
        // 3. 执行 SQL Query Pipeline
        // =====================================================

        log.info("");
        log.info("========== Step 2: Execute SQL Pipeline ==========");

        QueryResult result =
                sqlQueryService.execute(context);


        // =====================================================
        // 4. 输出最终结果
        // =====================================================

        log.info("");
        log.info("========== Step 3: Query Result ==========");

        log.info("Success: {}", result.isSuccess());
        log.info("SQL:");
        log.info("{}", result.getSql());

        log.info("Cost: {} ms", result.getCostMs());

        if (result.isSuccess()) {

            log.info("Data:");
            log.info("{}", result.getData());

        } else {

            log.error("Error:");
            log.error("{}", result.getError());
        }


        // =====================================================
        // 5. 基础断言
        // =====================================================

        Assertions.assertNotNull(
                context,
                "QueryContext should not be null"
        );

        Assertions.assertNotNull(
                result,
                "QueryResult should not be null"
        );

        Assertions.assertNotNull(
                result.getSql(),
                "Generated SQL should not be null"
        );

        Assertions.assertTrue(
                result.isSuccess(),
                "SQL execution should succeed, error=");
    }
}
