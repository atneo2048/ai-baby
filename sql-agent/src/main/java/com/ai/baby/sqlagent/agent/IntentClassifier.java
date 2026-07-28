package com.ai.baby.sqlagent.agent;

import dev.langchain4j.service.SystemMessage;

public interface IntentClassifier {

  @SystemMessage("""
      你是一名意图分类器。
      请判断用户输入属于以下哪一类：
      SQL_QUERY
      SCHEMA_QUERY
      CHAT
      DANGEROUS
      只返回JSON：
      {
        "intentType":"SQL_QUERY",
        "reason":"......",
        "confidence":0.98,
        "aiAnalysis":true
      }
      不要输出Markdown。
      不要解释。
      """)
  String classify(String question);

}
