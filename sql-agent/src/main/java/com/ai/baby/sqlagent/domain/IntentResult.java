package com.ai.baby.sqlagent.domain;

import lombok.Data;

/**
 * 意图结果
 */
@Data
public class IntentResult {

    /**
     * 意图
     */
    private IntentType intentType;

    /**
     * 原始问题
     */
    private String question;

    /**
     * 判断原因
     */
    private String reason;

    /**
     * 置信度
     */
    private double confidence;

    /**
     * 是否来自AI
     */
    private boolean aiAnalysis;

}
