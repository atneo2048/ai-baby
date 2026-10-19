package com.ai.baby.sqlagent.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ai.baby.sqlagent.domain.SqlErrorType;
import com.ai.baby.sqlagent.service.SqlErrorClassifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class DefaultSqlErrorClassifierTest {

    @Autowired
    private SqlErrorClassifier classifier;

    @Test
    void shouldReturnUnknownWhenErrorIsNullOrBlank() {
        assertEquals(SqlErrorType.UNKNOWN, classifier.classify(null));
        assertEquals(SqlErrorType.UNKNOWN, classifier.classify(""));
        assertEquals(SqlErrorType.UNKNOWN, classifier.classify("   "));
    }

    @Test
    void shouldClassifyColumnNotFound() {
        assertEquals(SqlErrorType.COLUMN_NOT_FOUND,
                classifier.classify("Unknown column 'e.salary2' in 'field list'"));
    }

    @Test
    void shouldClassifyTableNotFound() {
        assertEquals(SqlErrorType.TABLE_NOT_FOUND,
                classifier.classify("Table 'test.employee2' doesn't exist"));
    }

    @Test
    void shouldClassifySqlSyntaxError() {
        assertEquals(SqlErrorType.SYNTAX_ERROR,
                classifier.classify("You have an error in your SQL syntax"));
        assertEquals(SqlErrorType.SYNTAX_ERROR,
                classifier.classify("SQL syntax error near 'FROM'"));
    }

    @Test
    void shouldClassifyPermissionError() {
        assertEquals(SqlErrorType.PERMISSION_ERROR,
                classifier.classify("Access denied for user 'app'@'localhost'"));
        assertEquals(SqlErrorType.PERMISSION_ERROR,
                classifier.classify("Permission denied"));
    }

    @Test
    void shouldClassifyConnectionError() {
        assertEquals(SqlErrorType.CONNECTION_ERROR,
                classifier.classify("Connection refused"));
    }

    @Test
    void shouldClassifyTimeoutError() {
        assertEquals(SqlErrorType.TIMEOUT, classifier.classify("Query timeout"));
        assertEquals(SqlErrorType.TIMEOUT, classifier.classify("The operation timed out"));
    }

    @Test
    void shouldReturnUnknownForUnrecognizedError() {
        assertEquals(SqlErrorType.UNKNOWN,
                classifier.classify("Unexpected database error"));
    }
}
