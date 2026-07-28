package com.ai.baby.sqlagent.llm;

import dev.langchain4j.model.chat.ChatModel;

public interface ChatModelFactory {
    
    ChatModel getChatModel();
}
