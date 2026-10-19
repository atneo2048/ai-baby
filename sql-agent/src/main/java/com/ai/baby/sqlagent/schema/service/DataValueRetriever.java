package com.ai.baby.sqlagent.schema.service;

import java.util.List;

import com.ai.baby.sqlagent.schema.domain.DataValueMatch;

public interface DataValueRetriever {

    List<DataValueMatch> retrieve(List<String> entities, List<String> candidateTables);

    List<DataValueMatch> retrieve(List<String> entities);
}