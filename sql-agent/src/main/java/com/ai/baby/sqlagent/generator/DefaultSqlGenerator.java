package com.ai.baby.sqlagent.generator;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.agent.SqlAgent;
import com.ai.baby.sqlagent.domain.AgentContext;
import com.ai.baby.sqlagent.domain.SqlGenerationResult;
import com.ai.baby.sqlagent.prompt.PromptBuilder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultSqlGenerator implements SqlGenerator {

    private final SqlAgent sqlAgent;

    private final PromptBuilder promptBuilder;

    @Override
    public SqlGenerationResult generate(
            AgentContext context) {

        String prompt = promptBuilder.build(context);

        return sqlAgent.generate(prompt);
    }

    @Override
    public SqlGenerationResult regenerate(
            AgentContext context,
            String previousSql,
            String error) {

        String prompt = """
                你是一名SQL专家。

                用户问题：
                %s

                数据库Schema：
                %s

                上一次生成的SQL：
                %s

                SQL执行失败原因：
                %s

                请根据错误原因修正SQL。

                要求：
                1. 只生成SELECT
                2. 不允许INSERT
                3. 不允许UPDATE
                4. 不允许DELETE
                5. 不允许DDL
                6. 必须使用提供的Schema
                7. 不要解释
                8. 只返回SQL
                """.formatted(
                context.getQuestion(),
                context.getSchemas(),
                previousSql,
                error);

        return sqlAgent.generate(prompt);
    }

}
