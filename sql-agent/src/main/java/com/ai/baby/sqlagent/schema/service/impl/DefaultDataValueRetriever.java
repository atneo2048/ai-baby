package com.ai.baby.sqlagent.schema.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.schema.domain.ColumnInfo;
import com.ai.baby.sqlagent.schema.domain.DataValueMatch;
import com.ai.baby.sqlagent.schema.domain.SchemaInfo;
import com.ai.baby.sqlagent.schema.service.DataValueRetriever;
import com.ai.baby.sqlagent.schema.service.SchemaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultDataValueRetriever implements DataValueRetriever {

    private final SchemaService schemaService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<DataValueMatch> retrieve(List<String> entities, List<String> candidateTables) {

        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        if (candidateTables == null || candidateTables.isEmpty()) {
            return List.of();
        }

        log.info("Start data value retrieval, entities={}, candidateTables={}", entities, candidateTables);

        List<DataValueMatch> results = new ArrayList<>();

        for (String tableName : candidateTables) {
            SchemaInfo schema = schemaService.getSchema(tableName);

            if (schema == null || schema.getColumns() == null || schema.getColumns().isEmpty()) {
                continue;
            }

            for (ColumnInfo column : schema.getColumns()) {
                if (!isTextColumn(column)) {
                    continue;
                }

                for (String entity : entities) {
                    DataValueMatch match = searchExactValue(entity, schema, column);
                    if (match != null) {
                        results.add(match);
                    }
                }
            }
        }

        log.info("Data value retrieval completed, matches={}", results);

        return results;
    }

    @Override
    public List<DataValueMatch> retrieve(List<String> entities) {

        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        List<SchemaInfo> schemas = schemaService.loadSchemaList();
        List<DataValueMatch> results = new ArrayList<>();

        for (String entity : entities) {
            for (SchemaInfo schema : schemas) {
                if (schema.getColumns() == null) {
                    continue;
                }

                for (ColumnInfo column : schema.getColumns()) {
                    if (!isTextColumn(column)) {
                        continue;
                    }

                    DataValueMatch match = searchExactValue(entity, schema, column);
                    if (match != null) {
                        results.add(match);
                    }
                }
            }
        }

        return results;
    }

    private DataValueMatch searchExactValue(String entity, SchemaInfo schema, ColumnInfo column) {

        String tableName = schema.getTableName();
        String columnName = column.getColumnName();

        if (!isValidIdentifier(tableName) || !isValidIdentifier(columnName)) {
            return null;
        }

        String sql = String.format(
                "SELECT `%s` FROM `%s` WHERE `%s` = ? LIMIT 1",
                columnName, tableName, columnName);

        log.debug("Searching data value, table={}, column={}, entity={}", tableName, columnName, entity);

        try {
            List<String> values = jdbcTemplate.query(
                    sql,
                    ps -> ps.setString(1, entity),
                    (rs, rowNum) -> rs.getString(columnName));

            if (values.isEmpty()) {
                return null;
            }

            return DataValueMatch.builder()
                    .value(entity)
                    .tableName(tableName)
                    .columnName(columnName)
                    .matchedValue(values.get(0))
                    .score(10.0)
                    .reason("exact database value match")
                    .build();

        } catch (Exception e) {
            log.warn("Data value search failed: {}.{} = {}", tableName, columnName, entity, e);
            return null;
        }
    }

    private boolean isValidIdentifier(String value) {
        return value != null && value.matches("[a-zA-Z0-9_]+");
    }

    private boolean isTextColumn(ColumnInfo column) {

        if (column == null || column.getDataType() == null) {
            return false;
        }

        String type = column.getDataType().toLowerCase();
        return type.contains("char") || type.contains("text") || type.contains("varchar");
    }
}
