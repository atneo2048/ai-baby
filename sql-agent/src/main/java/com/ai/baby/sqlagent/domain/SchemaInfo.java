package com.ai.baby.sqlagent.domain;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SchemaInfo {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 表描述
     */
    private String comment;

    /**
     * 字段列表
     */
    private List<ColumnInfo> columns;
}
