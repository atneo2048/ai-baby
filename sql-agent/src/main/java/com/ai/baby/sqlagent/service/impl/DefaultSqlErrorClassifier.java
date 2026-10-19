package com.ai.baby.sqlagent.service.impl;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.ai.baby.sqlagent.domain.SqlErrorType;
import com.ai.baby.sqlagent.service.SqlErrorClassifier;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DefaultSqlErrorClassifier implements SqlErrorClassifier {

    @Override
    public SqlErrorType classify(String error) {

        if (error == null || error.isBlank()) {
            return SqlErrorType.UNKNOWN;
        }

        String message = error.toLowerCase(Locale.ROOT);

        if (message.contains("unknown column")) {
            return SqlErrorType.COLUMN_NOT_FOUND;
        }

        if (message.contains("table") && message.contains("doesn't exist")) {
            return SqlErrorType.TABLE_NOT_FOUND;
        }

        if (message.contains("syntax error")
                || message.contains("sql syntax")
                || message.contains("error in your sql syntax")) {
            return SqlErrorType.SYNTAX_ERROR;
        }

        if (message.contains("access denied") || message.contains("permission denied")) {
            return SqlErrorType.PERMISSION_ERROR;
        }

        if (message.contains("connection")) {
            return SqlErrorType.CONNECTION_ERROR;
        }

        if (message.contains("timeout") || message.contains("timed out")) {
            return SqlErrorType.TIMEOUT;
        }

        return SqlErrorType.UNKNOWN;
    }
}
