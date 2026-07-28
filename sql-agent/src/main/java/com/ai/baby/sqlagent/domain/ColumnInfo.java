package com.ai.baby.sqlagent.domain;

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
}
