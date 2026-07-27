package com.ai.baby.sqlagent.cache;

import org.springframework.stereotype.Component;

import com.ai.baby.sqlagent.domain.SchemaInfo;

@Component
public class SchemaCache {

    private SchemaInfo schemaInfo;

    /**
     * 获取缓存
     */
    public SchemaInfo get() {

        return schemaInfo;
    }

    /**
     * 更新缓存
     */
    public void put(SchemaInfo schemaInfo) {

        this.schemaInfo = schemaInfo;
    }

    /**
     * 是否存在
     */
    public boolean exists() {

        return schemaInfo != null;
    }

    /**
     * 清理缓存
     */
    public void clear() {

        schemaInfo = null;
    }
}
