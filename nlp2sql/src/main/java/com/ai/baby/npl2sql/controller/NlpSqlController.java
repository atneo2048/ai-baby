package com.ai.baby.npl2sql.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai.baby.npl2sql.entity.NlpSqlRequest;
import com.ai.baby.npl2sql.entity.NlpSqlResp;
import com.ai.baby.npl2sql.service.NlpSqlService;

@RestController
@RequestMapping("/chat")
public class NlpSqlController {

    @Autowired
    private NlpSqlService nlpSqlService;

    @RequestMapping("/send")
    public NlpSqlResp send(@RequestBody NlpSqlRequest request) {
        return nlpSqlService.send(request);
    }
}
