package com.ai.baby.sqlagent.schema.service;

import com.ai.baby.sqlagent.schema.domain.QueryContext;

public interface QueryContextBuilder {

    QueryContext build(String question);
}