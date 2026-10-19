package com.ai.baby.sqlagent.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai.baby.sqlagent.schema.service.SchemaService;

@RestController
@RequestMapping("/schema")
public class SchemaController {

    private final SchemaService schemaService;

    public SchemaController(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @PostMapping("/refresh")
    public String refresh() {
        schemaService.refresh();
        return "schema cache refreshed";
    }
}
