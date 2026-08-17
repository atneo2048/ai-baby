package com.ai.baby.sqlagent.schema;

import com.ai.baby.sqlagent.domain.SchemaQuery;

public interface SchemaQueryAnalyzer {

    SchemaQuery analyze(
            String question);
}
