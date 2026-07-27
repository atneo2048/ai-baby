package com.ai.baby.sqlagent.service;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.llm.ChatModelFactory;

import dev.langchain4j.model.chat.ChatModel;

@Service
public class ChatService {

    private final ChatModel chatModel;

    public ChatService(ChatModelFactory factory) {
        this.chatModel = factory.getChatModel();
    }

    public String chat(String message) {

        return chatModel.chat(message);
    }
}
