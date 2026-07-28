package com.ai.baby.sqlagent.prompt;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class PromptTemplateLoader {

    /**
     * 加载Prompt模板
     *  TODO 这里可以从数据库读取Prompt模板内容
     * @param name Prompt模板名称
     * @return Prompt模板内容
     */
    public String load(String name) {

        try {

            ClassPathResource resource = new ClassPathResource(
                    "prompts/" + name);
            return StreamUtils.copyToString(
                    resource.getInputStream(),
                    StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException(
                    "加载Prompt失败",
                    e);
        }
    }
}
