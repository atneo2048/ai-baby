package com.ai.baby.sqlagent.llm;

import java.time.Duration;

import org.springframework.stereotype.Component;

import com.ai.baby.sqlagent.config.LLMProperties;

import dev.langchain4j.http.client.apache.ApacheHttpClient;
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
        // 解决请求body为空的问题
        .httpClientBuilder(ApacheHttpClient.builder()
            .connectTimeout(Duration.ofMinutes(5))
            .readTimeout(Duration.ofMinutes(5)))
        .logRequests(true)
        .logResponses(true)
        .timeout(Duration.ofSeconds(properties.getTimeout()))
        .build();
  }

  @Override
  public ChatModel getChatModel() {
    return chatModel;
  }

}
