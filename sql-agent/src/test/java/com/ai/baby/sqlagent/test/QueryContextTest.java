package com.ai.baby.sqlagent.test;

import com.ai.baby.sqlagent.schema.domain.QueryContext;
import com.ai.baby.sqlagent.schema.service.QueryContextBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class QueryContextTest {

    @Autowired
    private QueryContextBuilder queryContextBuilder;


    @Test
    void testQueryContext() {

        QueryContext context =
                queryContextBuilder.build(
                        "查询研发部平均工资"
                );

        System.out.println(
                "========== Query Context =========="
        );

        System.out.println(
                "Question: "
                        + context.getQuestion()
        );

        System.out.println(
                "Schemas: "
                        + context.getSchemas()
        );

        System.out.println(
                "Entities: "
                        + context.getEntities()
        );

        System.out.println(
                "Data Values: "
                        + context.getDataValues()
        );

        System.out.println(
                "Relations: "
                        + context.getRelations()
        );

        System.out.println(
                "==================================="
        );
    }

}
