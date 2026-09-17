package com.ai.baby.sqlagent.generator;

import com.ai.baby.sqlagent.domain.AgentContext;
import com.ai.baby.sqlagent.domain.SqlGenerationResult;

public interface SqlGenerator {

    SqlGenerationResult generate(
            AgentContext context);

    SqlGenerationResult regenerate(
            AgentContext context,
            String previousSql,
            String error);
}
