package com.ai.baby.sqlagent.schema.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ColumnInfo {

    /**
     * 字段名
     */
    private String columnName;

    /**
     * 类型
     */
    private String dataType;

    /**
     * 描述
     */
    private String comment;

    /**
     * 是否允许 NULL
     */
    private Boolean nullable;

    /**
     * 是否主键
     */
    private Boolean primaryKey;
}
