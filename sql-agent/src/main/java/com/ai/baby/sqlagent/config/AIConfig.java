package com.ai.baby.sqlagent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ai.baby.sqlagent.agent.IntentClassifier;
import com.ai.baby.sqlagent.agent.SqlAgent;
import com.ai.baby.sqlagent.llm.ChatModelFactory;
import com.ai.baby.sqlagent.tool.DatabaseTool;
import com.ai.baby.sqlagent.tool.SchemaTool;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
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
            ChatModelFactory factory,
            DatabaseTool databaseTool,
            SchemaTool schemaTool) {
        return AiServices.builder(SqlAgent.class)
                .chatModel(factory.getChatModel())
                .tools(
                        schemaTool,
                        databaseTool)
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
