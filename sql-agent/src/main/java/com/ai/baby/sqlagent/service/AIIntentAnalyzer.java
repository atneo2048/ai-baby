package com.ai.baby.sqlagent.service;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.agent.IntentClassifier;
import com.ai.baby.sqlagent.domain.IntentResult;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AIIntentAnalyzer {

    private final IntentClassifier classifier;

    public AIIntentAnalyzer(IntentClassifier classifier) {
        this.classifier = classifier;
    }

    public IntentResult analyze(String question) throws JsonProcessingException {

        IntentResult result = classifier.classify(question);

        log.info("AI意图分析器结果: {}", result);
        result.setQuestion(question);
        return result;
    }
}