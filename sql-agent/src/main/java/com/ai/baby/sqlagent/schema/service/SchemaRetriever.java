package com.ai.baby.sqlagent.schema.service;

import java.util.List;

import com.ai.baby.sqlagent.schema.domain.SchemaInfo;
import com.ai.baby.sqlagent.schema.domain.SchemaRelation;

/**
 * 数据库Schema检索服务
 * 负责“我要哪些 Schema”问题
 */
public interface SchemaRetriever {

    /**
     * 根据问题检索直接相关的 Schema
     */
    List<SchemaInfo> retrieve(String question);

    /**
     * 根据候选表进行关系扩展
     */
    List<SchemaInfo> expandRelations(List<String> candidateTables);

    /**
     * 获取指定 Schema 之间的关系
     */
    List<SchemaRelation> getRelations(List<String> tables);

}