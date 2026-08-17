package com.ai.baby.sqlagent.tool;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.ai.baby.sqlagent.domain.QueryResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseTool {

    private final JdbcTemplate jdbcTemplate;

    public QueryResult executeSql(String sql) {

        long start = System.currentTimeMillis();

        try {

            List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);

            long cost = System.currentTimeMillis()
                    - start;

            return QueryResult.builder()
                    .sql(sql)
                    .data(data)
                    .success(true)
                    .costMs(cost)
                    .build();

        } catch (Exception e) {

            long cost = System.currentTimeMillis()
                    - start;

            return QueryResult.builder()
                    .sql(sql)
                    .success(false)
                    .costMs(cost)
                    .error(
                            e.getMessage())
                    .build();
        }
    }
}