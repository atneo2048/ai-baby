package com.ai.baby.sqlagent.schema.service.impl;

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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultQueryContextBuilder
        implements QueryContextBuilder {

    private final SchemaRetriever schemaRetriever;

    private final EntityExtractor entityExtractor;

    private final DataValueRetriever dataValueRetriever;

    @Override
    public QueryContext build(String question) {

        log.info("========== Building QueryContext ==========");
        log.info("Question: {}", question);

        // =====================================================
        // 1. 根据问题获取直接相关 Schema
        // =====================================================

        List<SchemaInfo> directSchemas =
                schemaRetriever.retrieve(question);

        log.info("Direct Schemas: {}", directSchemas);

        // =====================================================
        // 2. 提取具体业务实体
        // =====================================================

        List<String> entities =
                entityExtractor.extract(question);

        log.info("Entities: {}", entities);

        // =====================================================
        // 3. 到数据库中验证实体值
        // =====================================================

        List<DataValueMatch> dataValues =
                dataValueRetriever.retrieve(entities);

        log.info("Data Values: {}", dataValues);

        // =====================================================
        // 4. 汇总候选表
        // =====================================================

        Set<String> candidateTables =
                new LinkedHashSet<>();

        directSchemas.forEach(schema ->
                candidateTables.add(
                        schema.getTableName()
                )
        );

        dataValues.forEach(dataValue ->
                candidateTables.add(
                        dataValue.getTableName()
                )
        );

        log.info(
                "Schema candidates after data value retrieval: {}",
                candidateTables
        );

        // =====================================================
        // 5. 根据关系补齐中间表
        // =====================================================

        List<SchemaInfo> finalSchemas =
                schemaRetriever.expandRelations(
                        new ArrayList<>(candidateTables)
                );

        log.info(
                "Final Schemas: {}",
                finalSchemas.stream()
                        .map(SchemaInfo::getTableName)
                        .toList()
        );

        // =====================================================
        // 6. 获取最终 Schema 之间的关系
        // =====================================================

        List<String> finalTableNames =
                finalSchemas.stream()
                        .map(SchemaInfo::getTableName)
                        .toList();

        List<SchemaRelation> relations =
                schemaRetriever.getRelations(
                        finalTableNames
                );

        log.info(
                "Relations: {}",
                relations
        );

        // =====================================================
        // 7. 构建 QueryContext
        // =====================================================

        QueryContext context =
                QueryContext.builder()
                        .question(question)
                        .schemas(finalSchemas)
                        .entities(entities)
                        .dataValues(dataValues)
                        .relations(relations)
                        .build();

        log.info("========== QueryContext Completed ==========");

        return context;
    }
}