package com.ai.baby.sqlagent.domain;

public enum SqlErrorType {

    /**
     * SQL语法错误
     */
    SYNTAX_ERROR,

    /**
     * 表不存在
     */
    TABLE_NOT_FOUND,

    /**
     * 字段不存在
     */
    COLUMN_NOT_FOUND,

    /**
     * 数据库连接问题
     */
    CONNECTION_ERROR,

    /**
     * 权限问题
     */
    PERMISSION_ERROR,

    /**
     * SQL执行超时
     */
    TIMEOUT,

    /**
     * 未知错误
     */
    UNKNOWN
}
