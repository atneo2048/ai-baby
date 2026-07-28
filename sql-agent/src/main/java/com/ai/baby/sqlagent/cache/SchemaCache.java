package com.ai.baby.sqlagent.cache;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ai.baby.sqlagent.domain.SchemaInfo;

@Component
public class SchemaCache {

    private List<SchemaInfo> schemaList;

    /**
     * 获取缓存
     */
    public List<SchemaInfo> get() {

        return schemaList;
    }

    /**
     * 更新缓存
     */
    public void put(List<SchemaInfo> schemaList) {

        this.schemaList = schemaList;
    }

    /**
     * 是否存在
     */
    public boolean exists() {

        return schemaList != null;
    }

    /**
     * 清理缓存
     */
    public void clear() {

        schemaList = null;
    }
}
