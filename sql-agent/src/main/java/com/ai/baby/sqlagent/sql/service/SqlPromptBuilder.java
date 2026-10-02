package com.ai.baby.sqlagent.sql.service;

import com.ai.baby.sqlagent.schema.domain.QueryContext;

public interface SqlPromptBuilder {

    String build(QueryContext context);

}
