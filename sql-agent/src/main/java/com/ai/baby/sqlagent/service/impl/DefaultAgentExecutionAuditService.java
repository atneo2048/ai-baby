package com.ai.baby.sqlagent.service.impl;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.domain.AgentExecutionRecord;
import com.ai.baby.sqlagent.service.AgentExecutionAuditService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DefaultAgentExecutionAuditService implements AgentExecutionAuditService {

    @Override
    public void record(AgentExecutionRecord record) {
        log.info("[AGENT-AUDIT] {}", record);
    }
}
