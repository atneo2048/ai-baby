package com.ai.baby.sqlagent.schema.service;

import com.ai.baby.sqlagent.schema.domain.DataValueMatch;

import java.util.List;

public interface DataValueRetriever {

    List<DataValueMatch> retrieve(
            List<String> entities,
            List<String> candidateTables
    );

    List<DataValueMatch> retrieve(List<String> entities);
}