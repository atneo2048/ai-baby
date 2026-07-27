package com.ai.baby.npl2sql.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ai.baby.npl2sql.entity.NlpSqlRequest;
import com.ai.baby.npl2sql.entity.NlpSqlResp;
import com.ai.baby.npl2sql.prompt.SqlPrompt;
import com.ai.baby.npl2sql.service.DatabaseService;
import com.ai.baby.npl2sql.service.LlmService;
import com.ai.baby.npl2sql.service.NlpSqlService;

import okhttp3.MediaType;
import okhttp3.internal.http.HttpHeaders;

@Service
public class NlpSqlServiceImpl implements NlpSqlService {


    @Autowired
    private LlmService llm;

    @Autowired
    private DatabaseService database;

    @Override
    public NlpSqlResp send(NlpSqlRequest request) {
        NlpSqlResp resp = new NlpSqlResp();
        // 1.生成SQL
        String prompt = String.format(
                SqlPrompt.SQL_PROMPT,
                request.getQuestion());

        String sql = llm.ask(prompt);
        resp.setGeneratedSql(sql);

        // 2.执行SQL
        String data = database.execute(sql);

        // 3.总结结果
        String answerPrompt = "用户问题:%s \n数据库结果:%s 请用自然语言回答用户。".formatted(
                request.getQuestion(),
                data);

        String answer = llm.ask(
                answerPrompt);
        
        resp.setAnswer(answer);
        return resp;
    }
}
