package com.ai.baby.sqlagent.schema;

import java.util.List;

import com.ai.baby.sqlagent.domain.SchemaInfo;

/**
 * 数据库Schema缓存
 * 负责缓存数据库元数据，避免重复查询
 */
public interface SchemaCache {

    List<SchemaInfo> getAll();

    SchemaInfo get(String tableName);

    void put(SchemaInfo schema);

    void putAll(List<SchemaInfo> schemas);

    void clear();

    boolean isEmpty();
}