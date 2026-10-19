package com.ai.baby.sqlagent.schema.service;

/**
 * Schema 相关的通用工具方法。
 */
public final class SchemaNames {

    private SchemaNames() {
    }

    /**
     * 规范化表名/字段名：去首尾空白并转小写，便于大小写不敏感比较。
     */
    public static String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value.trim().toLowerCase();
    }
}
