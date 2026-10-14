package com.ai.baby.sqlagent.test;

import com.ai.baby.sqlagent.schema.domain.QueryContext;
import com.ai.baby.sqlagent.schema.service.QueryContextBuilder;
import com.ai.baby.sqlagent.sql.service.SqlGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SqlGeneratorTest {

    @Autowired
    private SqlGenerator sqlGenerator;

    @Autowired
    private QueryContextBuilder queryContextBuilder;

    @Test
    void generateSql() {

        String question =
                "查询研发部平均工资";

        QueryContext context =
                queryContextBuilder.build(question);

        String sql =
                sqlGenerator.generate(context);

        System.out.println(
                "========== Generated SQL =========="
        );

        System.out.println(sql);
    }
}