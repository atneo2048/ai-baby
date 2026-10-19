package com.ai.baby.sqlagent.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ai.baby.sqlagent.domain.GuardResult;
import com.ai.baby.sqlagent.enums.RiskLevel;
import com.ai.baby.sqlagent.guard.SqlGuard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals(RiskLevel.LOW, result.getRisk());
    }

    @Test
    void shouldBlockDelete() {
        GuardResult result = sqlGuard.check("DELETE FROM employee");
        assertFalse(result.isAllowed());
        assertEquals(RiskLevel.BLOCKED, result.getRisk());
    }

    @Test
    void shouldBlockUpdate() {
        GuardResult result = sqlGuard.check("UPDATE employee SET salary = 0");
        assertFalse(result.isAllowed());
    }

    @Test
    void shouldBlockDrop() {
        GuardResult result = sqlGuard.check("DROP TABLE employee");
        assertFalse(result.isAllowed());
    }

    @Test
    void shouldBlockEmptySql() {
        GuardResult result = sqlGuard.check("");
        assertFalse(result.isAllowed());
    }
}
