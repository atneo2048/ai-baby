package com.ai.baby.sqlagent.generator;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.agent.SqlAgent;
import com.ai.baby.sqlagent.domain.AgentContext;
import com.ai.baby.sqlagent.domain.SqlGenerationResult;
import com.ai.baby.sqlagent.prompt.PromptBuilder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeprecatedDefaultSqlGenerator implements SqlGenerator {

    private final SqlAgent sqlAgent;

    private final PromptBuilder promptBuilder;

    @Override
    public SqlGenerationResult generate(
            AgentContext context) {

        String prompt = promptBuilder.build(context);

        return sqlAgent.generate(prompt);
    }

    @Override
    public SqlGenerationResult regenerate(
            AgentContext context,
            String previousSql,
            String error) {

        String prompt = promptBuilder.reBuild(context,
                previousSql,
                error);

        return sqlAgent.generate(prompt);
    }

}
