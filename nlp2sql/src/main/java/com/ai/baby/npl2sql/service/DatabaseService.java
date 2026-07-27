package com.ai.baby.npl2sql.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String execute(String sql) {

        if (!sql
                .trim()
                .toUpperCase()
                .startsWith("SELECT")) {

            throw new RuntimeException(
                    "非法SQL");

        }

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);

        return result.toString();

    }

}