package com.ai.baby.sqlagent.tool;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;

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

    private final SchemaCache schemaCache;
    private final SchemaService schemaService;

    public SchemaTool(SchemaCache schemaCache, SchemaService schemaService) {
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
    public List<SchemaInfo> loadSchemaList() throws Exception {

        log.info("获取数据库结构");
        // 1. 查询缓存
        if (schemaCache.exists()) {
            return schemaCache
                    .get();
        }

        // 2、查询数据库结构
        List<SchemaInfo> schemaList = schemaService.loadSchemaList();
        
        // 3、缓存结果
        schemaCache.put(schemaList);
        
        return schemaList;
    }
}
