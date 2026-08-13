package com.ai.baby.sqlagent.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.agent.SqlAgent;
import com.ai.baby.sqlagent.analyzer.IntentAnalyzer;
import com.ai.baby.sqlagent.domain.AgentContext;
import com.ai.baby.sqlagent.domain.IntentResult;
import com.ai.baby.sqlagent.domain.SchemaInfo;
import com.ai.baby.sqlagent.domain.SqlGenerationResult;
import com.ai.baby.sqlagent.generator.SqlGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqlAgentService {

        private final IntentAnalyzer intentAnalyzer;

        private final SchemaRetriever schemaRetriever;

        private final SqlGenerator sqlGenerator;

        public SqlGenerationResult generate(String question) {

                // 1. 意图识别
                IntentResult intent = intentAnalyzer.analyze(question);

                // 2. Schema 检索
                List<SchemaInfo> schemas = schemaRetriever.retrieve(question);

                // 3. 构建 AgentContext
                AgentContext context = AgentContext.builder()
                                .question(question)
                                .intent(intent)
                                .schemas(schemas)
                                .databaseType("OceanBase")
                                .rules(List.of(
                                                "只允许SELECT",
                                                "禁止UPDATE",
                                                "禁止DELETE",
                                                "禁止DROP"))
                                .build();

                // 4. SQL生成
                return sqlGenerator.generate(context);
        }
}
