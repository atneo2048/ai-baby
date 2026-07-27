package com.ai.baby.sqlagent.service;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.agent.SqlAgent;

@Service
public class ChatService {

    private final SqlAgent sqlAgent;

    public ChatService(SqlAgent sqlAgent) {
        this.sqlAgent = sqlAgent;
    }

    public String chat(String message) {

        return sqlAgent.chat(message);
    }
}
