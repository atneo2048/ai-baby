package com.ai.baby.sqlagent.sql.service.impl;

import com.ai.baby.sqlagent.schema.domain.*;
import com.ai.baby.sqlagent.sql.service.SqlPromptBuilder;
import org.springframework.stereotype.Service;

@Service
public class DefaultSqlPromptBuilder implements SqlPromptBuilder {

    @Override
    public String build(QueryContext context) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                你是一个 SQL 生成器。

                你的任务是根据用户问题和数据库上下文生成一条可执行的 MySQL 查询 SQL。

                """);

        // 用户问题
        prompt.append("【用户问题】\n");
        prompt.append(context.getQuestion());
        prompt.append("\n\n");

        // Schema
        prompt.append("【数据库表】\n");

        for (SchemaInfo schema : context.getSchemas()) {

            prompt.append("表：")
                    .append(schema.getTableName())
                    .append("\n");

            prompt.append("说明：")
                    .append(schema.getComment())
                    .append("\n");

            prompt.append("字段：\n");

            if (schema.getColumns() != null) {

                for (ColumnInfo column :
                        schema.getColumns()) {

                    prompt.append("- ")
                            .append(column.getColumnName())
                            .append(" ")
                            .append(column.getDataType());

                    if (column.getComment() != null
                            && !column.getComment().isBlank()) {

                        prompt.append(" ")
                                .append(column.getComment());
                    }

                    prompt.append("\n");
                }
            }

            prompt.append("\n");
        }

        // Relations
        prompt.append("【表关系】\n");

        if (context.getRelations() != null) {

            for (SchemaRelation relation :
                    context.getRelations()) {

                prompt.append(relation.getFromTable())
                        .append(".")
                        .append(relation.getFromColumn())
                        .append(" = ")
                        .append(relation.getToTable())
                        .append(".")
                        .append(relation.getToColumn())
                        .append("\n");
            }
        }

        prompt.append("\n");

        // Data Values
        prompt.append("【已验证的数据值】\n");

        for (DataValueMatch dataValue :
                context.getDataValues()) {

            prompt.append(dataValue.getTableName())
                    .append(".")
                    .append(dataValue.getColumnName())
                    .append(" = '")
                    .append(dataValue.getMatchedValue())
                    .append("'\n");
        }

        prompt.append("\n");

        // Rules
        prompt.append("""
                【SQL 生成规则】

                1. 只能使用上述提供的表和字段。
                2. 只能使用上述提供的表关系进行 JOIN。
                3. 不允许虚构表名。
                4. 不允许虚构字段名。
                5. 不允许修改数据。
                6. 只生成 SELECT 查询。
                7. 不允许生成 INSERT、UPDATE、DELETE、DROP、ALTER 等语句。
                8. 返回 SQL，不要返回 Markdown。
                9. 不要返回解释。

                请直接输出 SQL。
                """);

        return prompt.toString();
    }
}