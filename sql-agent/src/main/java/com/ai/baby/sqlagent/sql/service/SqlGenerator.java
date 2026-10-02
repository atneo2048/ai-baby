package com.ai.baby.sqlagent.sql.service;

import com.ai.baby.sqlagent.schema.domain.QueryContext;

public interface SqlGenerator {

    String generate(QueryContext context);

}