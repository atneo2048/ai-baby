package com.ai.baby.sqlagent.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.agent.SqlAgent;
import com.ai.baby.sqlagent.domain.AgentContext;
import com.ai.baby.sqlagent.domain.IntentResult;
import com.ai.baby.sqlagent.domain.SchemaInfo;
import com.ai.baby.sqlagent.prompt.PromptBuilder;
import com.ai.baby.sqlagent.prompt.PromptContext;
import com.ai.baby.sqlagent.tool.SchemaTool;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SqlAgentService {

    private final SqlAgent sqlAgent;

    private final PromptBuilder promptBuilder;

    private final IntentAnalyzer intentAnalyzer;

    private final SchemaRetriever schemaRetriever;

    public SqlAgentService(
            SqlAgent sqlAgent,
            PromptBuilder promptBuilder,

            IntentAnalyzer intentAnalyzer,
            SchemaRetriever schemaRetriever) {

        this.sqlAgent = sqlAgent;
        this.promptBuilder = promptBuilder;
        this.intentAnalyzer = intentAnalyzer;
        this.schemaRetriever = schemaRetriever;
    }

    public Object ask(String question) throws Exception {

        IntentResult intent = intentAnalyzer.analyze(question);

        log.info("intent: {}", intent);

        List<SchemaInfo> schemas = schemaRetriever.retrieve(question);

        log.info("schemas: {}", schemas);

        AgentContext context = AgentContext.builder()
                .question(question)
                .intent(intent)
                .schemas(schemas)
                .databaseType(
                        "OceanBase")
                .rules(List.of(
                        "只允许SELECT",
                        "禁止修改数据",
                        "限制返回100条"))
                .build();

        String prompt = promptBuilder.build(context);
        return sqlAgent.chat(prompt);
    }
}
