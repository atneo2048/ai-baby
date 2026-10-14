package com.ai.baby.sqlagent.test;

import com.ai.baby.sqlagent.domain.AgentExecutionContext;
import com.ai.baby.sqlagent.domain.ReflectionDecision;
import com.ai.baby.sqlagent.domain.SqlErrorType;
import com.ai.baby.sqlagent.service.ReflectionService;
import com.ai.baby.sqlagent.service.SqlErrorClassifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class DefaultReflectionServiceTest {

    @Autowired
    private ReflectionService reflectionService;

    @Test
    void shouldRetryWhenColumnNotFound() {
        AgentExecutionContext context = buildContext(
                SqlErrorType.COLUMN_NOT_FOUND,
                "Unknown column 'e.salary2' in 'field list'"
        );

        ReflectionDecision decision =
                reflectionService.reflect(context);

        assertTrue(decision.isShouldRetry());
        assertNotNull(decision.getReason());
        assertNotNull(decision.getFeedback());
        assertTrue(decision.getFeedback().contains("COLUMN_NOT_FOUND"));
        assertTrue(decision.getFeedback().contains("salary2"));
    }

    @Test
    void shouldRetryWhenTableNotFound() {
        AgentExecutionContext context = buildContext(
                SqlErrorType.TABLE_NOT_FOUND,
                "Table 'test.employee2' doesn't exist"
        );

        ReflectionDecision decision =
                reflectionService.reflect(context);

        assertTrue(decision.isShouldRetry());
        assertNotNull(decision.getFeedback());
        assertTrue(decision.getFeedback().contains("TABLE_NOT_FOUND"));
    }

    @Test
    void shouldRetryWhenSqlSyntaxError() {
        AgentExecutionContext context = buildContext(
                SqlErrorType.SYNTAX_ERROR,
                "You have an error in your SQL syntax"
        );

        ReflectionDecision decision =
                reflectionService.reflect(context);

        assertTrue(decision.isShouldRetry());
        assertNotNull(decision.getFeedback());
        assertTrue(decision.getFeedback().contains("SYNTAX_ERROR"));
    }

    @Test
    void shouldNotRetryWhenConnectionError() {
        assertNotRetry(
                SqlErrorType.CONNECTION_ERROR,
                "Connection refused"
        );
    }

    @Test
    void shouldNotRetryWhenPermissionError() {
        assertNotRetry(
                SqlErrorType.PERMISSION_ERROR,
                "Access denied for user"
        );
    }

    @Test
    void shouldNotRetryWhenTimeout() {
        assertNotRetry(
                SqlErrorType.TIMEOUT,
                "Query timed out"
        );
    }

    @Test
    void shouldNotRetryWhenErrorTypeIsUnknown() {
        assertNotRetry(
                SqlErrorType.UNKNOWN,
                "Unexpected database error"
        );
    }

    @Test
    void shouldNotRetryWhenErrorTypeIsNull() {
        AgentExecutionContext context = buildContext(
                null,
                "Some SQL error"
        );

        ReflectionDecision decision =
                reflectionService.reflect(context);

        assertFalse(decision.isShouldRetry());
        assertNotNull(decision.getReason());
        assertNull(decision.getFeedback());
    }

    @Test
    void shouldRejectNullContext() {
        assertThrows(
                IllegalArgumentException.class,
                () -> reflectionService.reflect(null)
        );
    }

    private void assertNotRetry(
            SqlErrorType errorType,
            String error) {

        AgentExecutionContext context =
                buildContext(errorType, error);

        ReflectionDecision decision =
                reflectionService.reflect(context);

        assertFalse(decision.isShouldRetry());
        assertNotNull(decision.getReason());
        assertNull(decision.getFeedback());
    }

    private AgentExecutionContext buildContext(
            SqlErrorType errorType,
            String error) {

        return AgentExecutionContext.builder()
                .executionId("test-execution")
                .question("查询研发部的平均工资")
                .lastErrorType(errorType)
                .error(error)
                .retryCount(0)
                .build();
    }
}
