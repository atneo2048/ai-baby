package com.ai.baby.sqlagent.util;

/**
 * LLM 响应清洗工具。
 *
 * 统一处理大模型返回中常见的噪音：
 * <think> 推理块、Markdown 代码围栏，并提取出 JSON 或纯文本。
 */
public final class LlmResponseSanitizer {

    private LlmResponseSanitizer() {
    }

    /**
     * 移除 <think>...</think> 推理块。
     * 若 <think> 没有闭合标签，则从 <think> 之后截断。
     */
    public static String removeThinkBlock(String text) {

        if (text == null) {
            return "";
        }

        int start = text.indexOf("<think>");
        int end = text.indexOf("</think>");

        if (start >= 0 && end >= 0 && end > start) {
            return text.substring(0, start)
                    + text.substring(end + "</think>".length());
        }

        if (start >= 0) {
            return text.substring(0, start);
        }

        return text;
    }

    /**
     * 移除 Markdown 代码围栏（```json / ```sql / ```）。
     */
    public static String stripMarkdownFence(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("```json", "")
                .replace("```JSON", "")
                .replace("```sql", "")
                .replace("```SQL", "")
                .replace("```", "")
                .trim();
    }

    /**
     * 从文本中提取第一个 { 到最后一个 } 之间的 JSON 字符串。
     */
    public static String extractJson(String content) {

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("LLM 返回为空");
        }

        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");

        if (start < 0 || end < start) {
            throw new IllegalArgumentException("LLM 返回中不存在 JSON");
        }

        return content.substring(start, end + 1);
    }

    /**
     * 完整清洗 LLM 响应并提取 JSON。
     */
    public static String sanitizeToJson(String rawResponse) {

        String clean = removeThinkBlock(rawResponse);
        clean = stripMarkdownFence(clean);
        return extractJson(clean);
    }

    /**
     * 清洗 LLM 响应为纯文本（去 think、去 markdown）。
     */
    public static String sanitizeToText(String rawResponse) {

        String clean = removeThinkBlock(rawResponse);
        return stripMarkdownFence(clean);
    }
}
