package com.ai.baby.sqlagent.schema.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.ai.baby.sqlagent.domain.SchemaInfo;
import com.ai.baby.sqlagent.schema.SchemaCache;



@Component
public class LocalSchemaCache
        implements SchemaCache {

    private final Map<String, SchemaInfo> cache = new ConcurrentHashMap<>();

    @Override
    public List<SchemaInfo> getAll() {

        return new ArrayList<>(
                cache.values());
    }

    @Override
    public SchemaInfo get(
            String tableName) {

        return cache.get(
                tableName);
    }

    @Override
    public void put(
            SchemaInfo schema) {

        cache.put(
                schema.getTableName(),
                schema);
    }

    @Override
    public void putAll(
            List<SchemaInfo> schemas) {

        if (schemas == null) {
            return;
        }

        schemas.forEach(
                this::put);
    }

    @Override
    public void clear() {

        cache.clear();
    }

    @Override
    public boolean isEmpty() {
        return cache.isEmpty();
    }
}
