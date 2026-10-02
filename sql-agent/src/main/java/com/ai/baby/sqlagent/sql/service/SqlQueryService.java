package com.ai.baby.sqlagent.sql.service;

import com.ai.baby.sqlagent.domain.QueryResult;
import com.ai.baby.sqlagent.schema.domain.QueryContext;

public interface SqlQueryService {

    QueryResult execute(QueryContext context);
}