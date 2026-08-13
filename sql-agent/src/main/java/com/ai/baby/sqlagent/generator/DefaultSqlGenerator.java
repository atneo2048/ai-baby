package com.ai.baby.sqlagent.generator;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.agent.SqlAgent;
import com.ai.baby.sqlagent.domain.AgentContext;
import com.ai.baby.sqlagent.domain.SqlGenerationResult;
import com.ai.baby.sqlagent.parser.SqlGenerationResultParser;
import com.ai.baby.sqlagent.prompt.PromptBuilder;

@Service
public class DefaultSqlGenerator implements SqlGenerator {

    private final SqlAgent sqlAgent;

    private final PromptBuilder promptBuilder;

    private final SqlGenerationResultParser parser;

    public DefaultSqlGenerator(
            SqlAgent sqlAgent,
            PromptBuilder promptBuilder,
            SqlGenerationResultParser parser) {

        this.sqlAgent = sqlAgent;
        this.promptBuilder = promptBuilder;
        this.parser = parser;
    }

    @Override
    public SqlGenerationResult generate(
            AgentContext context) {

        String prompt =
                promptBuilder.build(context);

        return sqlAgent.generate(prompt);
    }


}
