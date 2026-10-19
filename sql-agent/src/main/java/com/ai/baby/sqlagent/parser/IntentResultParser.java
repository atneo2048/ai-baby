package com.ai.baby.sqlagent.parser;

import org.springframework.stereotype.Component;

import com.ai.baby.sqlagent.domain.IntentResult;
import com.ai.baby.sqlagent.util.LlmResponseSanitizer;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.model.openai.internal.chat.ChatCompletionResponse;

@Component
public class IntentResultParser implements Parser<IntentResult> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public IntentResult parse(String response) {

        try {
            ChatCompletionResponse completion = mapper.readValue(response, ChatCompletionResponse.class);

            if (completion.choices() == null || completion.choices().isEmpty()) {
                throw new IllegalArgumentException("LLM 返回 choices 为空");
            }

            String content = completion.choices().get(0).message().content();
            String json = LlmResponseSanitizer.sanitizeToJson(content);

            return mapper.readValue(json, IntentResult.class);
        } catch (Exception e) {
            throw new RuntimeException("解析 IntentResult 失败", e);
        }
    }
}
