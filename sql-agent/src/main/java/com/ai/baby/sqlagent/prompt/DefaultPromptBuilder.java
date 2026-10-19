package com.ai.baby.sqlagent.prompt;

import com.ai.baby.sqlagent.formatter.SchemaFormatter;
import com.ai.baby.sqlagent.schema.domain.QueryContext;
import org.springframework.stereotype.Service;

@Service
public class DefaultPromptBuilder implements PromptBuilder {

    private final PromptTemplateLoader loader;
    private final SchemaFormatter formatter;

    public DefaultPromptBuilder(PromptTemplateLoader loader, SchemaFormatter formatter) {
        this.loader = loader;
        this.formatter = formatter;
    }

    @Override
    public String build(QueryContext context) {

        String template = loader.load("sql-agent.txt");

        return template
                .replace("{{database}}", context.getDatabaseType())
                .replace("{{schema}}", formatter.format(context.getSchemas()))
                .replace("{{question}}", context.getQuestion())
                .replace("{{feedback}}", buildFeedbackSection(context));
    }

    private String buildFeedbackSection(QueryContext context) {

        if (context.getFeedback() == null || context.getFeedback().isBlank()) {
            return "";
        }

        return "上一次生成的 SQL 执行失败，请根据以下反馈重新生成正确的 SQL：\n"
                + context.getFeedback() + "\n";
    }
}
