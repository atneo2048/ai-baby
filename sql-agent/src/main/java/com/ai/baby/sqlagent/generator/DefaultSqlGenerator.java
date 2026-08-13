package com.ai.baby.sqlagent.generator;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.agent.SqlAgent;
import com.ai.baby.sqlagent.domain.AgentContext;
import com.ai.baby.sqlagent.domain.SqlGenerationResult;
import com.ai.baby.sqlagent.prompt.PromptBuilder;

@Service
public class DefaultSqlGenerator implements SqlGenerator {

    private final SqlAgent sqlAgent;

    private final PromptBuilder promptBuilder;

    public DefaultSqlGenerator(
            SqlAgent sqlAgent,
            PromptBuilder promptBuilder) {

        this.sqlAgent = sqlAgent;
        this.promptBuilder = promptBuilder;
    }

    @Override
    public SqlGenerationResult generate(
            AgentContext context) {

        String prompt =
                promptBuilder.build(context);

        return sqlAgent.generate(prompt);
    }


}
