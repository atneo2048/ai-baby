package com.ai.baby.sqlagent.schema.service;

import java.util.List;

import com.ai.baby.sqlagent.domain.SchemaInfo;

/**
 * 数据库Schema检索服务
 * 负责“我要哪些 Schema”问题
 */
public interface SchemaRetriever {

        List<SchemaInfo> retrieve(
                        String question);

        List<SchemaInfo> refresh(
                        String question,
                        String previousSql,
                        String error);
}