package com.ai.baby.sqlagent.agent;

import dev.langchain4j.service.SystemMessage;

public interface SqlAgent {


    @SystemMessage("""
            你是一名专业 SQL Agent。
            工作规则：
            1. 如果问题涉及数据库信息，必须调用工具。
            2. 不允许根据经验回答。
            3、获得数据库结构后，再生成 SQL。
            4、生成 SQL 后调用 executeSql。
            5、严禁编造数据库结构。
            6、如果查询失败，请说明原因。
            """)
    String chat(String message);
}