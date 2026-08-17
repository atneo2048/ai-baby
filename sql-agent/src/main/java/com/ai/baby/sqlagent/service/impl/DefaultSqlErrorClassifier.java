package com.ai.baby.sqlagent.service.impl;

import com.ai.baby.sqlagent.domain.SqlErrorType;
import com.ai.baby.sqlagent.service.SqlErrorClassifier;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Slf4j
@Component
public class DefaultSqlErrorClassifier
        implements SqlErrorClassifier {

    @Override
    public SqlErrorType classify(
            String error) {

        if (error == null
                || error.isBlank()) {

            return SqlErrorType.UNKNOWN;
        }

        String message = error.toLowerCase(
                Locale.ROOT);

        // 字段不存在
        if (message.contains(
                "unknown column")) {

            return SqlErrorType.COLUMN_NOT_FOUND;
        }

        // 表不存在
        if (message.contains(
                "table")
                && message.contains(
                        "doesn't exist")) {

            return SqlErrorType.TABLE_NOT_FOUND;
        }

        // SQL语法错误
        if (message.contains(
                "syntax error")
                || message.contains(
                        "sql syntax")) {

            return SqlErrorType.SYNTAX_ERROR;
        }

        // 权限
        if (message.contains(
                "access denied")
                || message.contains(
                        "permission denied")) {

            return SqlErrorType.PERMISSION_ERROR;
        }

        // 连接
        if (message.contains(
                "connection")) {

            return SqlErrorType.CONNECTION_ERROR;
        }

        // 超时
        if (message.contains(
                "timeout")
                || message.contains(
                        "timed out")) {

            return SqlErrorType.TIMEOUT;
        }

        return SqlErrorType.UNKNOWN;
    }
}
