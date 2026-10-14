package com.ai.baby.sqlagent.schema.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ai.baby.sqlagent.schema.domain.SchemaRelation;
import org.springframework.stereotype.Component;

import com.ai.baby.sqlagent.schema.domain.SchemaInfo;
import com.ai.baby.sqlagent.schema.service.SchemaCache;

@Component
public class LocalSchemaCache implements SchemaCache {

    private final Map<String, SchemaInfo> cache = new ConcurrentHashMap<>();
    /**
     * 表关系缓存
     */
    private volatile List<SchemaRelation> relations = List.of();

    @Override
    public List<SchemaInfo> getAll() {

        return new ArrayList<>(
                cache.values());
    }

    @Override
    public SchemaInfo get(String tableName) {

        return cache.get(
                tableName);
    }

    @Override
    public void put(SchemaInfo schema) {

        cache.put(
                schema.getTableName(),
                schema);
    }

    @Override
    public void putAll(List<SchemaInfo> schemas) {

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

    @Override
    public Integer size() {
        return cache.size();
    }

    @Override
    public void putRelations(List<SchemaRelation> relations) {

        if (relations == null || relations.isEmpty()) {
            this.relations = List.of();
            return;
        }

        this.relations = List.copyOf(relations);
    }

    @Override
    public List<SchemaRelation> getRelations(String tableName)  {

        if (tableName == null || tableName.isBlank()) {
            return List.of();
        }

        String normalizedTable = normalize(tableName);

        return relations.stream()
                .filter(relation ->
                        normalizedTable.equals(normalize(relation.getFromTable()))
                                || normalizedTable.equals(normalize(relation.getToTable()))
                )
                .toList();
    }

    @Override
    public List<SchemaRelation> getRelations() {
        return relations;
    }

    /**
     * 统一表名格式
     */
    private String normalize(String tableName) {

        return tableName
                .trim()
                .toLowerCase();
    }
}
