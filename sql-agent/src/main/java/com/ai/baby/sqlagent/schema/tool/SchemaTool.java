package com.ai.baby.sqlagent.schema.tool;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ai.baby.sqlagent.domain.SchemaInfo;
import com.ai.baby.sqlagent.schema.SchemaCache;
import com.ai.baby.sqlagent.schema.service.SchemaService;

import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据库Schema工具
 * 让 LLM 主动调用 Schema 查询能力
 */
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
        if (!schemaCache.isEmpty()) {
            return schemaCache.getAll();
        }

        // 2、查询数据库结构
        List<SchemaInfo> schemaList = schemaService.loadSchemaList();
        
        // 3、缓存结果
        schemaCache.putAll(schemaList);
        
        return schemaList;
    }
}
