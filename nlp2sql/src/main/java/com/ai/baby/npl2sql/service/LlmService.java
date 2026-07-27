package com.ai.baby.npl2sql.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LlmService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${nlp.api-key}")
    private String apiKey;

    @Value("${nlp.url}")
    private String url;

    public String ask(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        Map<String, Object> body = new HashMap<>();
        body.put("model", "qwen-plus");
        body.put("messages", List.of(
                Map.of("role", "user", "content", prompt)));
        HttpEntity entity = new HttpEntity(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url, entity,
                Map.class);

        Map result = response.getBody();

        List choices = (List) result.get("choices");

        Map first = (Map) choices.get(0);

        Map message = (Map) first.get("message");

        return message.get("content")
                .toString();

    }

}