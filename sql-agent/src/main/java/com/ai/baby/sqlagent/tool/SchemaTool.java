package com.ai.baby.sqlagent.tool;

import java.sql.ResultSet;
import java.time.LocalDateTime;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import org.springframework.stereotype.Component;

import com.ai.baby.sqlagent.cache.SchemaCache;
import com.ai.baby.sqlagent.domain.SchemaInfo;
import com.ai.baby.sqlagent.service.SchemaService;

import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SchemaTool {

    private final DataSource dataSource;
    private final SchemaCache schemaCache;
    private final SchemaService schemaService;

    public SchemaTool(DataSource dataSource, SchemaCache schemaCache, SchemaService schemaService) {
        this.dataSource = dataSource;
        this.schemaCache = schemaCache;
        this.schemaService = schemaService;
    }

    @PostConstruct
    public void init() {
        log.info("SchemaTool loaded");
    }

    @Tool("""
            获取当前数据库所有表、字段、类型信息。
            当用户询问数据库数据、
            查询业务指标、
            生成SQL之前，
            必须首先调用该工具。
            """)
    public String getSchema() throws Exception {

        log.info("获取数据库结构");
        // 1. 查询缓存
        if (schemaCache.exists()) {
            return schemaCache
                    .get()
                    .getSchema();
        }

        // 2、查询数据库结构
        String schema = schemaService.loadSchema();
        
        // 3、缓存结果
        schemaCache.put(
                SchemaInfo.builder()
                        .schema(schema)
                        .timestamp(LocalDateTime.now()).build());
        return schema;
    }
}
