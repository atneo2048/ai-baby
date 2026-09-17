package com.ai.baby.sqlagent.service;

import com.ai.baby.sqlagent.domain.SqlErrorType;

public interface SqlErrorClassifier {

    SqlErrorType classify(String error);
}