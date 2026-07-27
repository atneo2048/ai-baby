package com.ai.baby.sqlagent.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

@Service
public class SchemaService {

    private final DataSource dataSource;
    private final SchemaPermissionService permissionService;

    public SchemaService(DataSource dataSource, SchemaPermissionService permissionService) {
        this.dataSource = dataSource;
        this.permissionService = permissionService;
    }

    public String loadSchema() {

        StringBuilder result = new StringBuilder();
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

                result.append(
                        "表:")
                        .append(table)
                        .append("\n");
                ResultSet columns = meta.getColumns(
                        null,
                        null,
                        table,
                        "%");
                while (columns.next()) {
                    
                    String column = columns.getString("COLUMN_NAME");
                    if (!permissionService.allowColumn(column)) {
                        continue;
                    }

                    result.append("  ")
                            .append(column)
                            .append(" ")
                            .append(
                                    columns.getString(
                                            "TYPE_NAME"))
                            .append("\n");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result.toString();
    }
}
