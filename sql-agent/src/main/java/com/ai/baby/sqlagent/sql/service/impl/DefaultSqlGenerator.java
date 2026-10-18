package com.ai.baby.sqlagent.sql.service.impl;

import com.ai.baby.sqlagent.domain.SqlGenerationResult;
import com.ai.baby.sqlagent.llm.ChatModelFactory;
import com.ai.baby.sqlagent.parser.Parser;
import com.ai.baby.sqlagent.prompt.PromptBuilder;
import com.ai.baby.sqlagent.schema.domain.QueryContext;
import com.ai.baby.sqlagent.sql.service.SqlGenerator;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultSqlGenerator implements SqlGenerator {

    private final ChatModelFactory chatModelFactory;

    private final PromptBuilder promptBuilder;

    private final Parser<SqlGenerationResult> parser;

    @Override
    public SqlGenerationResult generate(QueryContext context) {

        if (context == null) {
            throw new IllegalArgumentException(
                    "QueryContext must not be null"
            );
        }

        String prompt = promptBuilder.build(context);

        log.info("========== SQL Generation ==========");
        log.info("SQL Prompt:\n{}", prompt);

        ChatModel chatModel = chatModelFactory.getChatModel();

        String response = chatModel.chat(prompt);

        log.info("LLM SQL Response:\n{}", response);
        return parser.parse(response);
    }
}