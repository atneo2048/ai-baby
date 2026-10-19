package com.ai.baby.sqlagent.sql.service;

import com.ai.baby.sqlagent.domain.SqlGenerationResult;
import com.ai.baby.sqlagent.schema.domain.QueryContext;

public interface SqlGenerator {

    SqlGenerationResult generate(QueryContext context);
}