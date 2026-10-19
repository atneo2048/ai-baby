package com.ai.baby.sqlagent.schema.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.schema.domain.DataValueMatch;
import com.ai.baby.sqlagent.schema.domain.QueryContext;
import com.ai.baby.sqlagent.schema.domain.SchemaInfo;
import com.ai.baby.sqlagent.schema.domain.SchemaRelation;
import com.ai.baby.sqlagent.schema.service.DataValueRetriever;
import com.ai.baby.sqlagent.schema.service.EntityExtractor;
import com.ai.baby.sqlagent.schema.service.QueryContextBuilder;
import com.ai.baby.sqlagent.schema.service.SchemaRetriever;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultQueryContextBuilder implements QueryContextBuilder {

    private final SchemaRetriever schemaRetriever;
    private final EntityExtractor entityExtractor;
    private final DataValueRetriever dataValueRetriever;

    @Override
    public QueryContext build(String question) {

        log.info("========== Building QueryContext ==========");
        log.info("Question: {}", question);

        List<SchemaInfo> directSchemas = schemaRetriever.retrieve(question);
        log.info("Direct Schemas: {}", directSchemas);

        List<String> entities = entityExtractor.extract(question);
        log.info("Entities: {}", entities);

        List<DataValueMatch> dataValues = dataValueRetriever.retrieve(entities);
        log.info("Data Values: {}", dataValues);

        Set<String> candidateTables = new LinkedHashSet<>();
        directSchemas.forEach(schema -> candidateTables.add(schema.getTableName()));
        dataValues.forEach(dataValue -> candidateTables.add(dataValue.getTableName()));

        log.info("Schema candidates after data value retrieval: {}", candidateTables);

        List<SchemaInfo> finalSchemas = schemaRetriever.expandRelations(new ArrayList<>(candidateTables));
        log.info("Final Schemas: {}", finalSchemas.stream()
                .map(SchemaInfo::getTableName)
                .toList());

        List<String> finalTableNames = finalSchemas.stream()
                .map(SchemaInfo::getTableName)
                .toList();

        List<SchemaRelation> relations = schemaRetriever.getRelations(finalTableNames);
        log.info("Relations: {}", relations);

        QueryContext context = QueryContext.builder()
                .question(question)
                .databaseType("MySQL")
                .schemas(finalSchemas)
                .entities(entities)
                .dataValues(dataValues)
                .relations(relations)
                .build();

        log.info("========== QueryContext Completed ==========");

        return context;
    }
}
