package com.ai.baby.sqlagent.agent;

import com.ai.baby.sqlagent.domain.SqlGenerationResult;

public interface SqlAgent {

    SqlGenerationResult generate(String prompt);
}