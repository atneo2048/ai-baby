package com.ai.baby.npl2sql.entity;

import lombok.Data;

@Data
public class NlpSqlRequest {
    
    /** 用户自然语言问题 */
    private String question;
    /** 会话ID，用于多轮对话扩展 */
    private String sessionId;
}
