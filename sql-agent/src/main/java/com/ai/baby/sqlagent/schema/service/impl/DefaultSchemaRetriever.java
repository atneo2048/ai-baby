package com.ai.baby.sqlagent.schema.service.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.schema.domain.ColumnInfo;
import com.ai.baby.sqlagent.schema.domain.SchemaInfo;
import com.ai.baby.sqlagent.schema.domain.SchemaKeyword;
import com.ai.baby.sqlagent.schema.domain.SchemaMatch;
import com.ai.baby.sqlagent.schema.domain.SchemaRelation;
import com.ai.baby.sqlagent.schema.service.SchemaCache;
import com.ai.baby.sqlagent.schema.service.SchemaNames;
import com.ai.baby.sqlagent.schema.service.SchemaRetriever;
import com.ai.baby.sqlagent.schema.tool.SchemaKeywordDictionary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultSchemaRetriever implements SchemaRetriever {

    private final SchemaCache schemaCache;
    private final SchemaKeywordDictionary keywordDictionary;

    private static final double MIN_SCORE = 5.0;
    private static final double COLUMN_MATCH_SCORE = 3.0;
    private static final double TABLE_MATCH_SCORE = 8.0;
    private static final double TABLE_COMMENT_SCORE = 6.0;
    private static final double COLUMN_COMMENT_SCORE = 4.0;

    @Override
    public List<SchemaInfo> retrieve(String question) {

        log.info("Start schema retrieval, question={}", question);

        if (question == null || question.isBlank()) {
            log.warn("Question is empty");
            return List.of();
        }

        List<SchemaInfo> schemas = schemaCache.getAll();
        if (schemas.isEmpty()) {
            log.warn("Schema cache is empty");
            return List.of();
        }

        List<SchemaKeyword> matchedKeywords = keywordDictionary.match(question);
        log.info("Business keywords: {}", matchedKeywords.stream()
                .map(SchemaKeyword::getKeyword)
                .distinct()
                .toList());

        List<SchemaMatch> matches = matchSchemas(question, schemas, matchedKeywords);

        matches.forEach(match -> log.info("Schema match: table={}, score={}, reasons={}",
                match.getSchema().getTableName(), match.getScore(), match.getReasons()));

        List<SchemaMatch> selectedMatches = selectMatches(matches);

        Set<String> candidateTables = selectedMatches.stream()
                .map(match -> match.getSchema().getTableName())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        log.info("Direct schema candidates: {}", candidateTables);

        // 按 Schema 原始顺序返回直接匹配的表；
        // 关系路径补全交由 expandRelations() 统一处理，避免重复计算。
        List<SchemaInfo> result = schemas.stream()
                .filter(schema -> candidateTables.contains(SchemaNames.normalize(schema.getTableName())))
                .toList();

        log.info("Schema retrieval completed, selected={}", result.stream()
                .map(SchemaInfo::getTableName)
                .toList());

        return result;
    }

    /**
     * 根据候选表补全它们之间需要经过的关系路径（BFS 最短路径）。
     */
    @Override
    public List<SchemaInfo> expandRelations(List<String> candidateTables) {

        log.info("Start schema relation expansion, candidateTables={}", candidateTables);

        if (candidateTables == null || candidateTables.isEmpty()) {
            return List.of();
        }

        List<SchemaInfo> schemas = schemaCache.getAll();
        if (schemas.isEmpty()) {
            return List.of();
        }

        Set<String> finalTables = expandRelationshipPaths(new LinkedHashSet<>(candidateTables));

        List<SchemaInfo> result = schemas.stream()
                .filter(schema -> finalTables.contains(SchemaNames.normalize(schema.getTableName())))
                .toList();

        log.info("Schema relation expansion completed, selected={}", result.stream()
                .map(SchemaInfo::getTableName)
                .toList());

        return result;
    }

    @Override
    public List<SchemaRelation> getRelations(List<String> tables) {

        if (tables == null || tables.isEmpty()) {
            return List.of();
        }

        List<SchemaRelation> allRelations = schemaCache.getRelations();
        if (allRelations == null || allRelations.isEmpty()) {
            return List.of();
        }

        Set<String> targetTables = tables.stream()
                .filter(table -> table != null && !table.isBlank())
                .map(SchemaNames::normalize)
                .collect(Collectors.toSet());

        return allRelations.stream()
                .filter(relation -> {
                    String fromTable = SchemaNames.normalize(relation.getFromTable());
                    String toTable = SchemaNames.normalize(relation.getToTable());
                    return targetTables.contains(fromTable) && targetTables.contains(toTable);
                })
                .toList();
    }

    private List<SchemaMatch> matchSchemas(String question, List<SchemaInfo> schemas,
            List<SchemaKeyword> matchedKeywords) {

        List<SchemaMatch> matches = new ArrayList<>();

        for (SchemaInfo schema : schemas) {
            double score = 0;
            List<String> reasons = new ArrayList<>();

            for (SchemaKeyword keyword : matchedKeywords) {
                if (!equalsTable(schema.getTableName(), keyword.getTableName())) {
                    continue;
                }

                score += keyword.getWeight();

                if (keyword.getColumnName() != null) {
                    reasons.add(String.format("business keyword matched: %s -> %s.%s",
                            keyword.getKeyword(), keyword.getTableName(), keyword.getColumnName()));
                } else {
                    reasons.add(String.format("business keyword matched: %s -> %s",
                            keyword.getKeyword(), keyword.getTableName()));
                }
            }

            if (containsIgnoreCase(question, schema.getTableName())) {
                score += TABLE_MATCH_SCORE;
                reasons.add("table matched: " + schema.getTableName());
            }

            if (containsIgnoreCase(question, schema.getComment())) {
                score += TABLE_COMMENT_SCORE;
                reasons.add("table comment matched: " + schema.getComment());
            }

            if (schema.getColumns() != null) {
                for (ColumnInfo column : schema.getColumns()) {
                    if (containsIgnoreCase(question, column.getColumnName())) {
                        score += COLUMN_MATCH_SCORE;
                        reasons.add("column matched: " + column.getColumnName());
                    }

                    if (containsIgnoreCase(question, column.getComment())) {
                        score += COLUMN_COMMENT_SCORE;
                        reasons.add("column comment matched: " + column.getColumnName());
                    }
                }
            }

            if (score >= MIN_SCORE) {
                matches.add(SchemaMatch.builder()
                        .schema(schema)
                        .score(score)
                        .reasons(reasons)
                        .build());
            }
        }

        matches.sort(Comparator.comparingDouble(SchemaMatch::getScore).reversed());
        return matches;
    }

    private List<SchemaMatch> selectMatches(List<SchemaMatch> matches) {

        if (matches.isEmpty()) {
            return List.of();
        }

        return matches.stream()
                .filter(match -> match.getScore() >= MIN_SCORE)
                .toList();
    }

    private Set<String> expandRelationshipPaths(Set<String> candidateTables) {

        if (candidateTables == null || candidateTables.isEmpty()) {
            return new LinkedHashSet<>();
        }

        List<SchemaRelation> relations = schemaCache.getRelations();
        if (relations == null || relations.isEmpty()) {
            return normalizeTables(candidateTables);
        }

        Map<String, Set<String>> graph = buildGraph(relations);
        Set<String> result = normalizeTables(candidateTables);
        List<String> candidates = new ArrayList<>(result);

        for (int i = 0; i < candidates.size(); i++) {
            for (int j = i + 1; j < candidates.size(); j++) {
                String start = candidates.get(i);
                String target = candidates.get(j);

                List<String> path = shortestPath(graph, start, target);
                if (path.isEmpty()) {
                    continue;
                }

                log.info("Relationship path: {} -> {}", start, target);
                log.info("Relationship path detail: {}", path);
                result.addAll(path);
            }
        }

        return result;
    }

    private Map<String, Set<String>> buildGraph(List<SchemaRelation> relations) {

        Map<String, Set<String>> graph = new HashMap<>();

        for (SchemaRelation relation : relations) {
            String from = SchemaNames.normalize(relation.getFromTable());
            String to = SchemaNames.normalize(relation.getToTable());

            graph.computeIfAbsent(from, key -> new LinkedHashSet<>()).add(to);
            graph.computeIfAbsent(to, key -> new LinkedHashSet<>()).add(from);
        }

        return graph;
    }

    private List<String> shortestPath(Map<String, Set<String>> graph, String start, String target) {

        start = SchemaNames.normalize(start);
        target = SchemaNames.normalize(target);

        if (start.equals(target)) {
            return List.of(start);
        }

        if (!graph.containsKey(start) || !graph.containsKey(target)) {
            return List.of();
        }

        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        Map<String, String> previous = new HashMap<>();

        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            Set<String> neighbors = graph.getOrDefault(current, Set.of());

            for (String neighbor : neighbors) {
                if (visited.contains(neighbor)) {
                    continue;
                }

                visited.add(neighbor);
                previous.put(neighbor, current);

                if (neighbor.equals(target)) {
                    return buildPath(previous, start, target);
                }

                queue.offer(neighbor);
            }
        }

        return List.of();
    }

    private List<String> buildPath(Map<String, String> previous, String start, String target) {

        LinkedListBuilder builder = new LinkedListBuilder();
        String current = target;

        while (current != null) {
            builder.addFirst(current);
            if (current.equals(start)) {
                break;
            }
            current = previous.get(current);
        }

        List<String> path = builder.toList();
        if (path.isEmpty() || !path.get(0).equals(start)) {
            return List.of();
        }

        return path;
    }

    private Set<String> normalizeTables(Set<String> tables) {

        if (tables == null) {
            return new LinkedHashSet<>();
        }

        return tables.stream()
                .filter(table -> table != null && !table.isBlank())
                .map(SchemaNames::normalize)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean equalsTable(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return left.equalsIgnoreCase(right);
    }

    private boolean containsIgnoreCase(String text, String target) {
        if (text == null || target == null || target.isBlank()) {
            return false;
        }
        return text.toLowerCase().contains(target.toLowerCase());
    }

    private static class LinkedListBuilder {

        private final LinkedList<String> list = new LinkedList<>();

        void addFirst(String value) {
            list.addFirst(value);
        }

        List<String> toList() {
            return new ArrayList<>(list);
        }
    }
}
