package com.ai.baby.sqlagent.schema.domain;

import lombok.Data;

import java.util.List;

@Data
public class ExtractedEntities {

    private List<String> entities;
}