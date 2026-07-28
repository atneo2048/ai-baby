package com.ai.baby.sqlagent.prompt;

import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String build(PromptContext context) {

        return String.format(
                PromptTemplate.SQL_AGENT,
                context.getSchema(),
                context.getSecurityRule(),
                context.getExamples(),
                context.getQuestion()
        );
    }
}
