package com.ai.baby.sqlagent.test;

import com.ai.baby.sqlagent.sql.service.SqlExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SqlExtractorTest {

    @Autowired
    private SqlExtractor sqlExtractor;

    @Test
    void extractThinkSql() {

        String response = """
                <think>
                用户需要查询研发部的平均工资。
                这里需要连接 employee、department、employee_salary。
                </think>

                SELECT AVG(es.salary + es.bonus)
                FROM employee e
                JOIN department d
                    ON e.department_id = d.id
                JOIN employee_salary es
                    ON e.id = es.employee_id
                WHERE d.department_name = '研发部';
                """;

        String sql = sqlExtractor.extract(response);

        System.out.println("========== Extracted SQL ==========");
        System.out.println(sql);
    }
}