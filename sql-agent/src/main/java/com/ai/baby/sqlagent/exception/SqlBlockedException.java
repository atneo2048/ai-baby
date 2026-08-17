package com.ai.baby.sqlagent.exception;

public class SqlBlockedException
        extends RuntimeException {

    public SqlBlockedException(
            String message) {

        super(message);
    }
}