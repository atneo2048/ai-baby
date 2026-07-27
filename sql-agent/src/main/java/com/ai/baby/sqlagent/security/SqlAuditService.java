package com.ai.baby.sqlagent.security;

import org.springframework.stereotype.Service;

import com.ai.baby.sqlagent.domain.SqlAuditRecord;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SqlAuditService {


    public void record(SqlAuditRecord record ){

        /*
         * 当前先打印日志
         *
         * 后续可以保存：
         *
         * MySQL
         * Elasticsearch
         * ClickHouse
         *
         */


        log.info(
                "SQL AUDIT:{}", record
        );
    }
}
