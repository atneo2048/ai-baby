package com.ai.baby.sqlagent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai.baby.sqlagent.domain.AgentResponse;
import com.ai.baby.sqlagent.dto.ChatRequest;
import com.ai.baby.sqlagent.service.SqlAgentService;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final SqlAgentService sqlAgentService;

    ChatController(SqlAgentService sqlAgentService) {
        this.sqlAgentService = sqlAgentService;
    }

    @GetMapping("/health")
    public String health() {
        return "SQL Agent V2 Running...";
    }

    @RequestMapping("/chat")
    public AgentResponse chat(@RequestBody ChatRequest request) throws Exception {
        
        return sqlAgentService.chat(request.getMessage());
    }
}
