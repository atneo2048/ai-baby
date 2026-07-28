package com.ai.baby.sqlagent.prompt;

import com.ai.baby.sqlagent.domain.AgentContext;
import com.ai.baby.sqlagent.formatter.SchemaFormatter;

import org.springframework.stereotype.Service;

@Service
public class DefaultPromptBuilder implements PromptBuilder {

    private final PromptTemplateLoader loader;

    private final SchemaFormatter formatter;

    public DefaultPromptBuilder(
            PromptTemplateLoader loader,
            SchemaFormatter formatter) {
        this.loader = loader;
        this.formatter = formatter;
    }

    @Override
    public String build(AgentContext context) {

        String template = loader.load(
                "sql-agent.txt");

        return template
                .replace(
                        "{{database}}",
                        context.getDatabaseType())
                .replace(
                        "{{schema}}",
                        formatter.format(
                                context.getSchemas()))
                .replace(
                        "{{question}}",
                        context.getQuestion())
                .replace(
                        "{{rules}}",
                        String.join(
                                "\n",
                                context.getRules()));
    }
}
