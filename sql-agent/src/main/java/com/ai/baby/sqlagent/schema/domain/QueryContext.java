package com.ai.baby.sqlagent.schema.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QueryContext {

    /**
     * 用户原始问题
     */
    private String question;

    /**
     * 本次查询涉及的 Schema
     */
    private List<SchemaInfo> schemas;

    /**
     * 从问题中提取出的具体业务实体
     */
    private List<String> entities;

    /**
     * 数据库中实际匹配到的业务值
     */
    private List<DataValueMatch> dataValues;

    /**
     * Schema 之间的关联关系
     */
    private List<SchemaRelation> relations;
}