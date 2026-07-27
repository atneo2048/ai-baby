package com.ai.baby.sqlagent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai.baby.sqlagent.dto.ChatRequest;
import com.ai.baby.sqlagent.dto.ChatResponse;
import com.ai.baby.sqlagent.service.ChatService;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/health")
public String health() {
    return "SQL Agent V2 Running...";
}

    @RequestMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String answer = chatService.chat(request.getMessage());
        return new ChatResponse(answer);
    }
}
