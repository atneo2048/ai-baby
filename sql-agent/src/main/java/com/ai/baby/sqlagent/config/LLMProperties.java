package com.ai.baby.sqlagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = LLMProperties.prefix)
public class LLMProperties {

    public static final String prefix = "llm";

    private String provider;

    private String baseUrl;

    private String apiKey;

    private String model;

    private Double temperature;

    private Integer timeout;

}
