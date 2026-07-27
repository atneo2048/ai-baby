package com.ai.baby.sqlagent.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SqlAuditRecord {

    /**
     * 用户问题
     */
    private String question;


    /**
     * 执行SQL
     */
    private String sql;


    /**
     * 是否成功
     */
    private boolean success;


    /**
     * 错误信息
     */
    private String error;


    /**
     * 执行耗时
     */
    private Long cost;


    /**
     * 时间
     */
    private LocalDateTime createTime;

}
