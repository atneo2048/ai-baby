package com.ai.baby.sqlagent.schema.service;

import java.util.List;

import com.ai.baby.sqlagent.schema.domain.SchemaInfo;
import com.ai.baby.sqlagent.schema.domain.SchemaRelation;

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

    Integer size();

    void putRelations(List<SchemaRelation> relations);

    List<SchemaRelation> getRelations(String tableName);

    List<SchemaRelation> getRelations();
}