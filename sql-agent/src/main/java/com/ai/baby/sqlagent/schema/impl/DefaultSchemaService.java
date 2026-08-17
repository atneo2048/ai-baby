package com.ai.baby.sqlagent.schema.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.domain.ColumnInfo;
import com.ai.baby.sqlagent.domain.SchemaInfo;
import com.ai.baby.sqlagent.schema.SchemaCache;
import com.ai.baby.sqlagent.schema.SchemaPermissionService;
import com.ai.baby.sqlagent.schema.SchemaService;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DefaultSchemaService implements SchemaService {

    private final DataSource dataSource;
    private final SchemaPermissionService permissionService;
    private final SchemaCache schemaCache;

    @PostConstruct
    public void init() {
        List<SchemaInfo> schemas = this.loadSchemaList();

        schemaCache.putAll(
                schemas);
    }

    @Override
    public List<SchemaInfo> loadSchemaList() {

        List<SchemaInfo> schemas = schemaCache.getAll();

        if (!schemas.isEmpty()) {
            return schemas;
        }

        // 查询数据库metadata
        List<SchemaInfo> schemaList = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();

            ResultSet tables = meta.getTables(
                    null,
                    null,
                    "%",
                    new String[] { "TABLE" });

            while (tables.next()) {

                String table = tables.getString("TABLE_NAME");

                if (!permissionService.allowTable(table)) {
                    continue;
                }

                ResultSet columns = meta.getColumns(
                        null,
                        null,
                        table,
                        "%");
                List<ColumnInfo> columnList = new ArrayList<>();
                while (columns.next()) {

                    String column = columns.getString("COLUMN_NAME");
                    if (!permissionService.allowColumn(column)) {
                        continue;
                    }

                    ColumnInfo columnInfo = ColumnInfo.builder()
                            .columnName(column)
                            .dataType(columns.getString("TYPE_NAME"))
                            .build();
                    columnList.add(columnInfo);
                }

                SchemaInfo schemaInfo = SchemaInfo.builder()
                        .tableName(table)
                        .columns(columnList)
                        .build();
                schemaList.add(schemaInfo);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        schemaCache.putAll(schemaList);

        return schemaList;
    }

    @Override
    public List<SchemaInfo> refresh() {
        return loadSchemaList();
    }
}
