package com.ai.baby.sqlagent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "message不能为空")
    private String message;

}
