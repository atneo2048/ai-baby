package com.ai.baby.sqlagent.schema.service;

import java.util.List;

public interface EntityExtractor {

    List<String> extract(String question);
}