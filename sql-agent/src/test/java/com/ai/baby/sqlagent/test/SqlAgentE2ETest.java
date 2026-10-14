package com.ai.baby.sqlagent.test;

import com.ai.baby.sqlagent.service.SqlAgentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SqlAgentE2ETest {

    @Autowired
    private SqlAgentService sqlAgentService;

    @Test
    void shouldAnswerUsingRealLlmAndDatabase() {
        String question = "查询研发部的平均工资";

        var response = sqlAgentService.chat(question);

        assertNotNull(response, "Agent 响应不能为 null");

        System.out.println("========== SQL Agent E2E ==========");
        System.out.println("Question: " + question);
        System.out.println("Response: " + response);
    }
}
