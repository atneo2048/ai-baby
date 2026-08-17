package com.ai.baby.sqlagent.schema.service.impl;


import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.domain.SchemaInfo;
import com.ai.baby.sqlagent.schema.SchemaCache;
import com.ai.baby.sqlagent.schema.service.SchemaRetriever;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultSchemaRetriever
                implements SchemaRetriever {

        private final SchemaCache schemaCache;

        @Override
        public List<SchemaInfo> retrieve(
                        String question) {

                List<SchemaInfo> all = schemaCache.getAll();

                return search(
                                question,
                                all);
        }

        @Override
        public List<SchemaInfo> refresh(
                        String question,
                        String previousSql,
                        String error) {

                /*
                 * 当前阶段：
                 * refresh = 重新从Cache检索
                 */

                List<SchemaInfo> all = schemaCache.getAll();

                return search(
                                question,
                                all);
        }

        private List<SchemaInfo> search(
                        String question,
                        List<SchemaInfo> schemas) {

                String q = question.toLowerCase(
                                Locale.ROOT);

                return schemas.stream()
                                .filter(schema -> match(q, schema))
                                .limit(10)
                                .toList();
        }

        private boolean match(
                        String question,
                        SchemaInfo schema) {

                if (contains(
                                question,
                                schema.getTableName())) {
                        return true;
                }

                if (contains(
                                question,
                                schema.getComment())) {
                        return true;
                }

                if (schema.getColumns() == null) {
                        return false;
                }

                return schema.getColumns()
                                .stream()
                                .anyMatch(column -> contains(
                                                question,
                                                column.getColumnName())
                                                ||
                                                contains(
                                                                question,
                                                                column.getComment()));
        }

        private boolean contains(
                        String text,
                        String target) {

                return target != null
                                && !target.isBlank()
                                && text.contains(
                                                target.toLowerCase(
                                                                Locale.ROOT));
        }
}