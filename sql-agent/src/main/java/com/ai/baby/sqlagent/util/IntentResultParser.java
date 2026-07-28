package com.ai.baby.sqlagent.util;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.model.openai.internal.chat.ChatCompletionResponse;

import com.ai.baby.sqlagent.domain.IntentResult;

@Component
public class IntentResultParser {

    private final ObjectMapper mapper = new ObjectMapper();

    public IntentResult parse(String response) {

        try {

            // 第一层
            ChatCompletionResponse completion = mapper.readValue(response, ChatCompletionResponse.class);

            if (completion.choices() == null || completion.choices().isEmpty()) {
                throw new IllegalArgumentException("LLM 返回 choices 为空");
            }

            String content = completion.choices()
                    .get(0)
                    .message()
                    .content();

            // 第二层
            return mapper.readValue(content, IntentResult.class);
        } catch (Exception e) {
            throw new RuntimeException("解析 IntentResult 失败", e);
        }
    }
}