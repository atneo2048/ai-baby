package com.ai.baby.sqlagent.domain;

/**
 * 意图类型
 */
public enum IntentType {

  /**
     * SQL查询
     */
    SQL_QUERY,

    /**
     * 查询Schema
     */
    SCHEMA_QUERY,

    /**
     * 普通聊天
     */
    CHAT,

    /**
     * 危险操作
     */
    DANGEROUS,

    /**
     * 未知
     */
    UNKNOWN

}
