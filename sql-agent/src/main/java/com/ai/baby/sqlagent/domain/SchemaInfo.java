package com.ai.baby.sqlagent.domain;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SchemaInfo {

    /**
     * 数据库结构文本
     */
    private String schema;


    /**
     * 更新时间
     */
    private LocalDateTime timestamp;
}
