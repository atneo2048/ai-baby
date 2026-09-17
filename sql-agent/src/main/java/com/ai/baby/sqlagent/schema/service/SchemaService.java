package com.ai.baby.sqlagent.schema.service;

import java.util.List;

import com.ai.baby.sqlagent.domain.SchemaInfo;

/**
 * 数据库Schema服务
 * 负责真正访问数据库元数据
 */
public interface SchemaService {

    List<SchemaInfo> loadSchemaList();

    List<SchemaInfo> refresh();
}