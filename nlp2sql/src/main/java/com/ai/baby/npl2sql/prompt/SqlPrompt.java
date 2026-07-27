package com.ai.baby.npl2sql.prompt;

public class SqlPrompt {

    public static String SQL_PROMPT = "你是一名MySQL专家。根据用户问题生成SQL。" +
   " 数据库：employee表:id" +
    "name" +
   "age" +
   "department" +
   "salary" +
   " " +
    "规则: 1.只能生成SELECT2.禁止DELETE UPDATE INSERT3.只返回SQL4.不要Markdown用户问题:%s";
}
