package com.ai.baby.sqlagent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ai.baby.sqlagent.agent.IntentClassifier;
import com.ai.baby.sqlagent.agent.SqlAgent;
import com.ai.baby.sqlagent.llm.ChatModelFactory;

import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class AIConfig {

    @PostConstruct
    public void init() {
        log.info("AIConfig loaded");
    }

    @Bean
    public SqlAgent sqlAgent(
            ChatModelFactory factory) {
        return AiServices.builder(SqlAgent.class)
                .chatModel(factory.getChatModel())
                .build();
    }

    @Bean
    public IntentClassifier intentClassifier(
            ChatModelFactory factory) {
        return AiServices.builder(IntentClassifier.class)
                .chatModel(factory.getChatModel())
                .build();
    }
}
