package com.ai.baby.sqlagent.schema.service.impl;

import com.ai.baby.sqlagent.llm.ChatModelFactory;
import com.ai.baby.sqlagent.schema.domain.ExtractedEntities;
import com.ai.baby.sqlagent.schema.service.EntityExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmEntityExtractor implements EntityExtractor {

    private final ChatModelFactory chatModelFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<String> extract(String question) {

        if (question == null
                || question.isBlank()) {

            return List.of();
        }

        String prompt = buildPrompt(question);

        log.info(
                "Entity extraction request, question={}",
                question
        );

        String response =
                chatModelFactory
                        .getChatModel()
                        .chat(prompt);;

        log.info(
                "Entity extraction raw response: {}",
                response
        );

        return parse(response);
    }

    private String buildPrompt(String question) {

        return """
                你是一个数据库查询系统中的实体提取器。

                你的任务是从用户的问题中提取：
                1. 具体的人名
                2. 部门名称
                3. 项目名称
                4. 地点
                5. 职位名称
                6. 公司或组织名称
                7. 其他可能对应数据库字段实际值的业务实体

                注意：
                - 只提取具体业务实体。
                - 不要提取“员工”“部门”“工资”“项目”等泛化概念。
                - 不要推测数据库表名。
                - 不要推测数据库字段名。
                - 不要修改用户原始实体。
                - 如果没有具体实体，返回空数组。
                - 必须只返回 JSON。
                
                JSON 格式：

                {
                  "entities": [
                    "实体1",
                    "实体2"
                  ]
                }

                用户问题：
                %s
                """.formatted(question);
    }

    private List<String> parse(String content) {

        try {

            String json = cleanJson(content);

            log.info(
                    "Cleaned entity extraction JSON: {}",
                    json
            );

            ExtractedEntities result =
                    objectMapper.readValue(
                            json,
                            ExtractedEntities.class
                    );

            if (result.getEntities() == null) {
                return List.of();
            }

            return result.getEntities()
                    .stream()
                    .filter(entity ->
                            entity != null
                                    && !entity.isBlank())
                    .map(String::trim)
                    .distinct()
                    .toList();

        } catch (Exception e) {

            log.error(
                    "Failed to parse entity extraction response: {}",
                    content,
                    e
            );

            return List.of();
        }
    }

    private String cleanJson(String content) {

        if (content == null
                || content.isBlank()) {

            return "";
        }

        String text = content.trim();

        // =====================================================
        // 1. 删除完整 think block
        // =====================================================

        text = text.replaceAll(
                "(?s)<think>.*?</think>",
                ""
        ).trim();

        // =====================================================
        // 2. 如果仍然存在 <think>，说明可能没有闭合
        //    那么直接从 <think> 后面截取
        // =====================================================

        int thinkStart =
                text.indexOf("<think>");

        if (thinkStart >= 0) {

            text = text.substring(
                    thinkStart + "<think>".length()
            ).trim();
        }

        // =====================================================
        // 3. 删除 Markdown Code Fence
        // =====================================================

        if (text.startsWith("```json")) {

            text = text.substring(7).trim();

        } else if (text.startsWith("```")) {

            text = text.substring(3).trim();
        }

        if (text.endsWith("```")) {

            text = text.substring(
                    0,
                    text.length() - 3
            ).trim();
        }

        // =====================================================
        // 4. 最后尝试从第一个 { 开始提取 JSON
        // =====================================================

        int jsonStart =
                text.indexOf("{");

        int jsonEnd =
                text.lastIndexOf("}");

        if (jsonStart >= 0
                && jsonEnd >= jsonStart) {

            text = text.substring(
                    jsonStart,
                    jsonEnd + 1
            );
        }

        return text.trim();
    }
}