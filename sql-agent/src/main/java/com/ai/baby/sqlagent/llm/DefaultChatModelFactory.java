package com.ai.baby.sqlagent.llm;

import java.time.Duration;

import org.springframework.stereotype.Component;

import com.ai.baby.sqlagent.config.LLMProperties;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

@Component
public class DefaultChatModelFactory implements ChatModelFactory {

      private final ChatModel chatModel;

    public DefaultChatModelFactory(LLMProperties properties) {
             this.chatModel = OpenAiChatModel.builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .modelName(properties.getModel())
                .temperature(properties.getTemperature())
                .timeout(Duration.ofSeconds(properties.getTimeout()))
                .build();
    }

    @Override
    public ChatModel getChatModel() {
        return chatModel;
    }

}
