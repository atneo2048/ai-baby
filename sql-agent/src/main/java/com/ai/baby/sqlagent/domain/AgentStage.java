package com.ai.baby.sqlagent.domain;

public enum AgentStage {

    INIT,

    INTENT,

    SCHEMA,

    CONTEXT,

    SQL_GENERATION,

    SQL_GUARD,

    SQL_EXECUTION,

    RESPONSE,

    COMPLETED,

    FAILED

}