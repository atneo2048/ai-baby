package com.ai.baby.sqlagent.prompt;

import com.ai.baby.sqlagent.schema.domain.QueryContext;

public interface PromptBuilder {

    String build(QueryContext context);
}
