package com.ai.baby.sqlagent.service;

import com.ai.baby.sqlagent.domain.AgentExecutionRecord;

public interface AgentExecutionAuditService {

    void record(AgentExecutionRecord record);

}