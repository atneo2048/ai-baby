package com.ai.baby.sqlagent.exception;

public class SqlExecutionException
        extends RuntimeException {

    public SqlExecutionException(
            String message) {

        super(message);
    }
}
