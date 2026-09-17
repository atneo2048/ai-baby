package com.ai.baby.sqlagent.test;

import com.ai.baby.sqlagent.guard.SqlGuard;
import com.ai.baby.sqlagent.domain.GuardResult;
import com.ai.baby.sqlagent.enums.RiskLevel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SqlGuardTest {

    @Autowired
    private SqlGuard sqlGuard;

    @Test
    void shouldAllowSelect() {

        String sql = """
                SELECT id, name
                FROM employee
                LIMIT 100
                """;

        GuardResult result = sqlGuard.check(sql);

        assertTrue(result.isAllowed());

        assertEquals(
                RiskLevel.LOW,
                result.getRisk());
    }

    @Test
    void shouldBlockDelete() {

        String sql = "DELETE FROM employee";

        GuardResult result = sqlGuard.check(sql);

        assertFalse(
                result.isAllowed());

        assertEquals(
                RiskLevel.BLOCKED,
                result.getRisk());
    }

    @Test
    void shouldBlockUpdate() {

        String sql = "UPDATE employee SET salary = 0";

        GuardResult result = sqlGuard.check(sql);

        assertFalse(
                result.isAllowed());
    }

    @Test
    void shouldBlockDrop() {

        String sql = "DROP TABLE employee";

        GuardResult result = sqlGuard.check(sql);

        assertFalse(
                result.isAllowed());
    }

    @Test
    void shouldBlockEmptySql() {

        GuardResult result = sqlGuard.check("");

        assertFalse(
                result.isAllowed());
    }

}