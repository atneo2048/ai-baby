package com.ai.baby.sqlagent.parser;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.model.openai.internal.chat.ChatCompletionResponse;

import com.ai.baby.sqlagent.domain.IntentResult;

@Component
public class IntentResultParser implements Parser<IntentResult> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
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

            content = normalize(content);
            String json = extractJson(content);
            // 第二层
            return mapper.readValue(json, IntentResult.class);
        } catch (Exception e) {
            throw new RuntimeException("解析 IntentResult 失败", e);
        }
    }

    private String extractJson(String text) {

        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start == -1 || end == -1) {
            throw new RuntimeException("未找到JSON");
        }
        return text.substring(start, end + 1);
    }

    private String normalize(String text) {

        if (text == null) {
            return "";
        }
        text = text.trim();
        text = text.replace("```json", "");
        text = text.replace("```", "");
        return text.trim();

    }
}