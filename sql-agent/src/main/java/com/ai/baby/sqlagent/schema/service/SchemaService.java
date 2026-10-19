package com.ai.baby.sqlagent.schema.service;

import java.util.List;

import com.ai.baby.sqlagent.schema.domain.SchemaInfo;
import com.ai.baby.sqlagent.schema.domain.SchemaRelation;

/**
 * 数据库Schema服务
 * 负责真正访问数据库元数据
 */
public interface SchemaService {

    /**
     * 获取 Schema
     */
    List<SchemaInfo> loadSchemaList();

    /**
     * 获取指定表
     */
    SchemaInfo getSchema(String tableName);

    /**
     * 获取所有表关系
     */
    List<SchemaRelation> loadRelations();

    /**
     * 强制刷新 Schema（清缓存并重新从数据库加载）
     */
    List<SchemaInfo> refresh();
}
