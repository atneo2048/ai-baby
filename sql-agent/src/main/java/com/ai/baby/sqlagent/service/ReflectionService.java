package com.ai.baby.sqlagent.service;

import com.ai.baby.sqlagent.domain.AgentExecutionContext;
import com.ai.baby.sqlagent.domain.ReflectionDecision;

public interface ReflectionService {

    ReflectionDecision reflect(AgentExecutionContext context);
}
