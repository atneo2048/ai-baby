package com.ai.baby.npl2sql.entity;

import lombok.Data;

@Data
public class NlpSqlResp {

    private Boolean success;
    private String msg;

    private String sessionId;
    private String generatedSql;
    
    private String answer;
}
