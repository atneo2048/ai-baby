package com.ai.baby.sqlagent.domain;

import com.ai.baby.sqlagent.enums.RiskLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuardResult {

    private boolean allowed;

    private RiskLevel risk;

    private String reason;
}
