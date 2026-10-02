package com.ai.baby.sqlagent.schema.service.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.ai.baby.sqlagent.schema.domain.SchemaRelation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.schema.domain.ColumnInfo;
import com.ai.baby.sqlagent.schema.domain.SchemaInfo;
import com.ai.baby.sqlagent.schema.service.SchemaCache;
import com.ai.baby.sqlagent.schema.service.SchemaService;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;

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
        log.info(
                "Schema initialization completed, schemaCount={}, relationCount={}",
                schemaCache.size(),
                schemaCache.getRelations().size()
        );
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

        try (Connection connection =
                     dataSource.getConnection()) {

            DatabaseMetaData metaData =
                    connection.getMetaData();

            String catalog =
                    connection.getCatalog();

            log.info(
                    "Loading schema relations, catalog={}",
                    catalog
            );

            // =====================================================
            // 1. 获取所有表
            // =====================================================

            List<String> tableNames = new ArrayList<>();

            try (ResultSet tables =
                         metaData.getTables(
                                 catalog,
                                 null,
                                 "%",
                                 new String[]{"TABLE"}
                         )) {

                while (tables.next()) {

                    String tableName =
                            tables.getString("TABLE_NAME");

                    if (tableName == null
                            || tableName.isBlank()) {
                        continue;
                    }

                    tableNames.add(tableName);
                }
            }

            log.info(
                    "Tables found for relation loading: {}",
                    tableNames
            );

            // =====================================================
            // 2. 逐表获取 Imported Keys
            // =====================================================

            for (String tableName : tableNames) {

                log.info(
                        "Loading relations for table={}",
                        tableName
                );

                try (ResultSet rs =
                             metaData.getImportedKeys(
                                     catalog,
                                     null,
                                     tableName
                             )) {

                    while (rs.next()) {

                        String pkTable =
                                rs.getString("PKTABLE_NAME");

                        String pkColumn =
                                rs.getString("PKCOLUMN_NAME");

                        String fkTable =
                                rs.getString("FKTABLE_NAME");

                        String fkColumn =
                                rs.getString("FKCOLUMN_NAME");

                        if (pkTable == null
                                || pkColumn == null
                                || fkTable == null
                                || fkColumn == null) {

                            continue;
                        }

                        SchemaRelation relation =
                                SchemaRelation.builder()
                                        .fromTable(fkTable)
                                        .fromColumn(fkColumn)
                                        .toTable(pkTable)
                                        .toColumn(pkColumn)
                                        .build();

                        relations.add(relation);

                        log.info(
                                "Schema relation: {}.{} -> {}.{}",
                                fkTable,
                                fkColumn,
                                pkTable,
                                pkColumn
                        );
                    }
                }
            }

        } catch (SQLException e) {

            log.error(
                    "Failed to load schema relations",
                    e
            );

            throw new IllegalStateException(
                    "Failed to load schema relations",
                    e
            );
        }

        // =========================================================
        // 3. 去重
        // =========================================================

        List<SchemaRelation> distinctRelations =
                relations.stream()
                        .distinct()
                        .toList();

        // =========================================================
        // 4. 写入 Cache
        // =========================================================

        schemaCache.putRelations(
                distinctRelations
        );

        log.info(
                "Schema relations loaded, count={}",
                distinctRelations.size()
        );

        return distinctRelations;
    }

    @Override
    public List<SchemaRelation> getRelations(String tableName) {

        if (tableName == null || tableName.isBlank()) {
            return List.of();
        }

        return schemaCache.getRelations(tableName);
    }

    /**
     * 从数据库中加载
     */
    private List<SchemaInfo> loadFromDatabase() {
        // 查询数据库metadata
        List<SchemaInfo> schemaList = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            String catalog = conn.getCatalog();

            log.info("Loading schema metadata, catalog={}", catalog);
            ResultSet tables = meta.getTables(
                    null,
                    null,
                    "%",
                    new String[] { "TABLE" });

            while (tables.next()) {

                String tableName = tables.getString("TABLE_NAME");

                List<ColumnInfo> columnList = loadColumns(meta, catalog, tableName);

                SchemaInfo schemaInfo = SchemaInfo.builder()
                        .tableName(tableName)
                        .comment(tables.getString("REMARKS"))
                        .columns(columnList)
                        .build();
                schemaList.add(schemaInfo);
            }
        } catch (Exception e) {
            log.error("failed to load database schema", e);
            throw new IllegalStateException("Failed to load database schema", e);
        }

        schemaCache.clear();
        schemaCache.putAll(schemaList);

        log.info( "Schema loaded successfully, tableCount={}", schemaList.size());
        return schemaList;
    }

    private static List<ColumnInfo> loadColumns(DatabaseMetaData meta, String catalog, String tableName) throws SQLException {

        List<ColumnInfo> columnList = new ArrayList<>();

        // 先获取主键
        List<String> primaryKeys = loadPrimaryKeys(meta, catalog, tableName);

        try (ResultSet columns = meta.getColumns(
                null,
                null,
                tableName,
                "%");){

            while (columns.next()) {

                val columnName = columns.getString("COLUMN_NAME");

                boolean primaryKey =
                        primaryKeys.contains(columnName);

                ColumnInfo columnInfo = ColumnInfo.builder()
                        .columnName(columnName)
                        .dataType(columns.getString("TYPE_NAME"))
                        .comment(columns.getString("REMARKS"))
                        .nullable("YES".equalsIgnoreCase(columns.getString("IS_NULLABLE")))
                        .primaryKey(primaryKey)
                        .build();
                columnList.add(columnInfo);
            }
        }

        return columnList;
    }

    /**
     * 获取主键
     */
    private static List<String> loadPrimaryKeys(DatabaseMetaData meta
            , String catalog, String tableName) throws SQLException {

        List<String> primaryKeys = new ArrayList<>();

        try (ResultSet resultSet =
                     meta.getPrimaryKeys(
                             catalog,
                             null,
                             tableName)) {

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
