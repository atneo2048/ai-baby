package com.ai.baby.sqlagent.service;

import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class SchemaPermissionService {

    /**
     * 当前允许访问的表
     */
    private final Set<String> allowTables = Set.of(
            "employee",
            "department",
            "salary");

    private final Set<String> hiddenColumns = Set.of(
            "password",
            "client_secret",
            "bank_no",
            "id_card",
            "phone");

    public boolean allowTable(String tableName) {

        return allowTables.contains(
                tableName.toLowerCase());
    }

    public boolean allowColumn(String column) {

        return !hiddenColumns.contains(
                column.toLowerCase());

    }
}
