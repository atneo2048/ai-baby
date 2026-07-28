package com.ai.baby.sqlagent.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.domain.ColumnInfo;
import com.ai.baby.sqlagent.domain.SchemaInfo;

@Service
public class SchemaService {

    private final DataSource dataSource;
    private final SchemaPermissionService permissionService;

    public SchemaService(DataSource dataSource, SchemaPermissionService permissionService) {
        this.dataSource = dataSource;
        this.permissionService = permissionService;
    }

    public List<SchemaInfo> loadSchemaList() {

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
        return schemaList;
    }
}
