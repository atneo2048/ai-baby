package com.ai.baby.sqlagent.domain;

import java.time.LocalDateTime;

import com.ai.baby.sqlagent.enums.RiskLevel;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentExecutionRecord {

    /**
     * 一次Agent请求唯一ID
     */
    private String executionId;

    /**
     * 用户问题
     */
    private String question;

    /**
     * Intent
     */
    private String intent;

    /**
     * Intent置信度
     */
    private Double confidence;

    /**
     * 生成SQL
     */
    private String sql;

    /**
     * SQL风险等级
     */
    private RiskLevel risk;

    /**
     * SQL Guard是否通过
     */
    private Boolean guardAllowed;

    /**
     * SQL Guard原因
     */
    private String guardReason;

    /**
     * 查询返回数据量
     */
    private Integer rowCount;

    /**
     * SQL执行耗时
     */
    private Long executionTimeMs;

    /**
     * Agent总耗时
     */
    private Long totalTimeMs;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 当前执行阶段
     */
    private AgentStage stage;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
