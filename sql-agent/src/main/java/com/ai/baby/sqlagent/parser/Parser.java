package com.ai.baby.sqlagent.parser;

public interface Parser<T> {
    
    T parse(String json);
}
