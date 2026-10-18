package com.ai.baby.sqlagent.prompt;

import com.ai.baby.sqlagent.domain.AgentContext;
import com.ai.baby.sqlagent.schema.domain.QueryContext;

public interface PromptBuilder {

        String build(QueryContext context);

        String reBuild(AgentContext context,
                String previousSql,
                String error);
}
