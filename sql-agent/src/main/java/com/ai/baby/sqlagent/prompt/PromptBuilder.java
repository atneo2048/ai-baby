package com.ai.baby.sqlagent.prompt;

import com.ai.baby.sqlagent.domain.AgentContext;

public interface PromptBuilder {

        String build(AgentContext context);
}
