package com.ai.baby.sqlagent.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.domain.SchemaInfo;

@Service
public class SchemaRetriever {

    private final SchemaService schemaService;

    public SchemaRetriever(
            SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    public List<SchemaInfo> retrieve(
            String question) {

        List<SchemaInfo> schemas = schemaService.loadSchemaList();

        return schemas.stream()
                .filter(schema -> match(schema, question))
                .toList();
    }

    private boolean match(
            SchemaInfo schema,
            String question) {

        String q = question.toLowerCase();

        if (q.contains("员工")
                && schema.getTableName()
                        .contains("employee")) {
            return true;

        }

        if (q.contains("部门")
                && schema.getTableName()
                        .contains("department")) {
            return true;

        }

        if (q.contains("工资")
                && schema.getColumns()
                        .stream()
                        .anyMatch(
                                c -> c.getColumnName()
                                        .contains("salary"))) {
            return true;
        }

        return false;

    }
}
