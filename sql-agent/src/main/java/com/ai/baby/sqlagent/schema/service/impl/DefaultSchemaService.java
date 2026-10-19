package com.ai.baby.sqlagent.schema.service.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.schema.domain.ColumnInfo;
import com.ai.baby.sqlagent.schema.domain.SchemaInfo;
import com.ai.baby.sqlagent.schema.domain.SchemaRelation;
import com.ai.baby.sqlagent.schema.service.SchemaCache;
import com.ai.baby.sqlagent.schema.service.SchemaService;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@Service
@AllArgsConstructor
public class DefaultSchemaService implements SchemaService {

    private final DataSource dataSource;
    private final SchemaCache schemaCache;

    @PostConstruct
    public void init() {
        log.info("Initializing schema service...");
        loadSchemaList();
        loadRelations();
        log.info("Schema initialization completed, schemaCount={}, relationCount={}",
                schemaCache.size(), schemaCache.getRelations().size());
    }

    @Override
    public List<SchemaInfo> loadSchemaList() {

        List<SchemaInfo> cacheSchemas = schemaCache.getAll();

        if (!cacheSchemas.isEmpty()) {
            log.info("schema loaded from cache, size={}", cacheSchemas.size());
            return cacheSchemas;
        }

        return loadFromDatabase();
    }

    @Override
    public SchemaInfo getSchema(String tableName) {
        return schemaCache.get(tableName);
    }

    @Override
    public List<SchemaRelation> loadRelations() {

        List<SchemaRelation> relations = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {

            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();

            log.info("Loading schema relations, catalog={}", catalog);

            List<String> tableNames = new ArrayList<>();

            try (ResultSet tables = metaData.getTables(catalog, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    if (tableName == null || tableName.isBlank()) {
                        continue;
                    }
                    tableNames.add(tableName);
                }
            }

            log.info("Tables found for relation loading: {}", tableNames);

            for (String tableName : tableNames) {
                log.info("Loading relations for table={}", tableName);

                try (ResultSet rs = metaData.getImportedKeys(catalog, null, tableName)) {
                    while (rs.next()) {
                        String pkTable = rs.getString("PKTABLE_NAME");
                        String pkColumn = rs.getString("PKCOLUMN_NAME");
                        String fkTable = rs.getString("FKTABLE_NAME");
                        String fkColumn = rs.getString("FKCOLUMN_NAME");

                        if (pkTable == null || pkColumn == null
                                || fkTable == null || fkColumn == null) {
                            continue;
                        }

                        relations.add(SchemaRelation.builder()
                                .fromTable(fkTable)
                                .fromColumn(fkColumn)
                                .toTable(pkTable)
                                .toColumn(pkColumn)
                                .build());

                        log.info("Schema relation: {}.{} -> {}.{}",
                                fkTable, fkColumn, pkTable, pkColumn);
                    }
                }
            }

        } catch (SQLException e) {
            log.error("Failed to load schema relations", e);
            throw new IllegalStateException("Failed to load schema relations", e);
        }

        List<SchemaRelation> distinctRelations = relations.stream().distinct().toList();
        schemaCache.putRelations(distinctRelations);

        log.info("Schema relations loaded, count={}", distinctRelations.size());

        return distinctRelations;
    }

    private List<SchemaInfo> loadFromDatabase() {

        List<SchemaInfo> schemaList = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            String catalog = conn.getCatalog();

            log.info("Loading schema metadata, catalog={}", catalog);

            try (ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    List<ColumnInfo> columnList = loadColumns(meta, catalog, tableName);

                    schemaList.add(SchemaInfo.builder()
                            .tableName(tableName)
                            .comment(tables.getString("REMARKS"))
                            .columns(columnList)
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("failed to load database schema", e);
            throw new IllegalStateException("Failed to load database schema", e);
        }

        schemaCache.clear();
        schemaCache.putAll(schemaList);

        log.info("Schema loaded successfully, tableCount={}", schemaList.size());
        return schemaList;
    }

    private static List<ColumnInfo> loadColumns(DatabaseMetaData meta, String catalog, String tableName)
            throws SQLException {

        List<ColumnInfo> columnList = new ArrayList<>();
        List<String> primaryKeys = loadPrimaryKeys(meta, catalog, tableName);

        try (ResultSet columns = meta.getColumns(null, null, tableName, "%")) {
            while (columns.next()) {
                val columnName = columns.getString("COLUMN_NAME");
                boolean primaryKey = primaryKeys.contains(columnName);

                columnList.add(ColumnInfo.builder()
                        .columnName(columnName)
                        .dataType(columns.getString("TYPE_NAME"))
                        .comment(columns.getString("REMARKS"))
                        .nullable("YES".equalsIgnoreCase(columns.getString("IS_NULLABLE")))
                        .primaryKey(primaryKey)
                        .build());
            }
        }

        return columnList;
    }

    private static List<String> loadPrimaryKeys(DatabaseMetaData meta, String catalog, String tableName)
            throws SQLException {

        List<String> primaryKeys = new ArrayList<>();

        try (ResultSet resultSet = meta.getPrimaryKeys(catalog, null, tableName)) {
            while (resultSet.next()) {
                primaryKeys.add(resultSet.getString("COLUMN_NAME"));
            }
        }

        return primaryKeys;
    }

    @Override
    public List<SchemaInfo> refresh() {
        log.info("Refreshing all database schema");
        return loadFromDatabase();
    }
}
