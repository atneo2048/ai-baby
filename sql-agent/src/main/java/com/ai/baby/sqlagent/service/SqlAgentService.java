package com.ai.baby.sqlagent.service;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.agent.SqlAgent;
import com.ai.baby.sqlagent.domain.IntentResult;
import com.ai.baby.sqlagent.prompt.PromptBuilder;
import com.ai.baby.sqlagent.prompt.PromptContext;
import com.ai.baby.sqlagent.tool.SchemaTool;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SqlAgentService {

    private final SqlAgent sqlAgent;

    private final PromptBuilder promptBuilder;

    private final SchemaTool schemaTool;

    private final IntentAnalyzer intentAnalyzer;

    public SqlAgentService(
            SqlAgent sqlAgent,
            PromptBuilder promptBuilder,
            SchemaTool schemaTool,
            IntentAnalyzer intentAnalyzer) {

        this.sqlAgent = sqlAgent;
        this.promptBuilder = promptBuilder;
        this.schemaTool = schemaTool;
        this.intentAnalyzer = intentAnalyzer;
    }

    public String ask(String question) throws Exception {

        IntentResult intent = intentAnalyzer.analyze(question);

        log.info("intent: {}", intent);

        switch (intent.getIntentType()) {

            case SQL_QUERY:
                return executeSql(question, schemaTool);

            case SCHEMA_QUERY:
                return schemaTool.getSchema();

            case CHAT:
                return sqlAgent.chat(question);

            case DANGEROUS:
                return "检测到危险操作，请修改您的问题。";
            default:
                return "暂时无法理解您的问题。";
        }
    }

    private String executeSql(String question, SchemaTool schemaTool) throws Exception {
      
        PromptContext context = new PromptContext();

        context.setQuestion(question);

        context.setSchema(schemaTool.getSchema());

        context.setSecurityRule(
                """
                        只能执行SELECT。
                        禁止修改数据。
                        """);

        context.setExamples(
                """
                        查询平均工资:
                        SELECT AVG(salary)
                        FROM employee;
                        """);
        String prompt = promptBuilder.build(context);
        return sqlAgent.chat(prompt);
    }

}
