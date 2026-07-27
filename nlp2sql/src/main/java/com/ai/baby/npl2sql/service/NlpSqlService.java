package com.ai.baby.npl2sql.service;

import com.ai.baby.npl2sql.entity.NlpSqlRequest;
import com.ai.baby.npl2sql.entity.NlpSqlResp;

public interface NlpSqlService {

    NlpSqlResp send(NlpSqlRequest request);
}
