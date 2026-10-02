package com.ai.baby.sqlagent.schema.service.impl;

import com.ai.baby.sqlagent.schema.domain.ColumnInfo;
import com.ai.baby.sqlagent.schema.domain.DataValueMatch;
import com.ai.baby.sqlagent.schema.domain.SchemaInfo;
import com.ai.baby.sqlagent.schema.service.DataValueRetriever;
import com.ai.baby.sqlagent.schema.service.SchemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultDataValueRetriever implements DataValueRetriever {

    private final SchemaService schemaService;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<DataValueMatch> retrieve(
            List<String> entities,
            List<String> candidateTables) {

        if (entities == null
                || entities.isEmpty()) {

            return List.of();
        }

        if (candidateTables == null
                || candidateTables.isEmpty()) {

            return List.of();
        }

        log.info(
                "Start data value retrieval, entities={}, candidateTables={}",
                entities,
                candidateTables
        );

        List<DataValueMatch> results =
                new ArrayList<>();

        for (String tableName : candidateTables) {

            SchemaInfo schema =
                    schemaService.getSchema(tableName);

            if (schema == null) {
                continue;
            }

            if (schema.getColumns() == null
                    || schema.getColumns().isEmpty()) {
                continue;
            }

            for (ColumnInfo column :
                    schema.getColumns()) {

                if (!isTextColumn(column)) {
                    continue;
                }

                for (String entity : entities) {

                    DataValueMatch match =
                            searchValue(
                                    entity,
                                    schema,
                                    column
                            );

                    if (match != null) {
                        results.add(match);
                    }
                }
            }
        }

        log.info(
                "Data value retrieval completed, matches={}",
                results
        );

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

                for (ColumnInfo column : schema.getColumns()) {

                    // 只搜索文本类型字段
                    if (!isTextColumn(column)) {
                        continue;
                    }

                    DataValueMatch match =
                            searchExactValue(entity, schema, column);

                    if (match != null) {
                        results.add(match);
                    }
                }
            }
        }

        return results;
    }

    private DataValueMatch searchExactValue(
            String entity,
            SchemaInfo schema,
            ColumnInfo column) {

        String tableName = schema.getTableName();
        String columnName = column.getColumnName();

        if (!isValidIdentifier(tableName)
                || !isValidIdentifier(columnName)) {
            return null;
        }

        String sql = String.format(
                "SELECT `%s` FROM `%s` WHERE `%s` = ? LIMIT 1",
                columnName,
                tableName,
                columnName
        );

        try {

            List<String> values = jdbcTemplate.query(
                    sql,
                    ps -> ps.setString(1, entity),
                    (rs, rowNum) -> rs.getString(1)
            );

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

            log.warn(
                    "Data value search failed: {}.{} = {}",
                    tableName,
                    columnName,
                    entity,
                    e
            );

            return null;
        }
    }

    private boolean isValidIdentifier(String value) {
        return value != null
                && value.matches("[a-zA-Z0-9_]+");
    }

    private DataValueMatch searchValue(
            String entity,
            SchemaInfo schema,
            ColumnInfo column) {

        String sql = """
                SELECT %s
                FROM %s
                WHERE %s = ?
                LIMIT 1
                """.formatted(
                quoteIdentifier(column.getColumnName()),
                quoteIdentifier(schema.getTableName()),
                quoteIdentifier(column.getColumnName())
        );

        log.debug(
                "Searching data value, table={}, column={}, entity={}",
                schema.getTableName(),
                column.getColumnName(),
                entity
        );

        try {

            List<String> values =
                    jdbcTemplate.query(
                            sql,
                            ps -> ps.setString(
                                    1,
                                    entity
                            ),
                            (rs, rowNum) ->
                                    rs.getString(
                                            column.getColumnName()
                                    )
                    );

            if (values.isEmpty()) {
                return null;
            }

            String matchedValue =
                    values.get(0);

            return DataValueMatch.builder()
                    .value(entity)
                    .tableName(schema.getTableName())
                    .columnName(column.getColumnName())
                    .matchedValue(matchedValue)
                    .score(10.0)
                    .reason("exact database value match")
                    .build();

        } catch (Exception e) {

            log.warn(
                    "Failed to search data value, table={}, column={}, entity={}",
                    schema.getTableName(),
                    column.getColumnName(),
                    entity,
                    e
            );

            return null;
        }
    }

    private boolean isTextColumn(
            ColumnInfo column) {

        if (column == null
                || column.getDataType() == null) {
            return false;
        }

        String type =
                column.getDataType()
                        .toLowerCase();

        return type.contains("char")
                || type.contains("text")
                || type.contains("varchar");
    }

    private String quoteIdentifier(
            String identifier) {

        if (identifier == null
                || identifier.isBlank()) {

            throw new IllegalArgumentException(
                    "Invalid identifier"
            );
        }

        if (!identifier.matches(
                "[a-zA-Z0-9_]+")) {

            throw new IllegalArgumentException(
                    "Invalid SQL identifier: "
                            + identifier
            );
        }

        return "`" + identifier + "`";
    }
}