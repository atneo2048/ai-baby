package com.ai.baby.sqlagent.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai.baby.sqlagent.cache.SchemaCache;

@RestController
@RequestMapping("/schema")
public class SchemaController {

    private final SchemaCache cache;

    public SchemaController(
            SchemaCache cache) {
        this.cache = cache;

    }

    @PostMapping("/refresh")
    public String refresh() {
        cache.clear();
        return "schema cache cleared";
    }
}
