package com.openstudy.ai.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON 解析器
 * 负责清洗、修复和解析 AI 返回的 JSON 内容
 */
@Slf4j
@Component
public class JsonParser {

    /**
     * 清洗内容（去除 Markdown 标记和控制字符）
     */
    public String clean(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        return content
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .replaceAll("[\u0000-\u001F\u007F-\u009F]", "")
                .trim();
    }

    /**
     * 修复 JSON 常见问题
     */
    public String repair(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String repaired = content;

        // 0. 移除 reasoning_content 字段（AI思考过程，不需要）
        repaired = repaired.replaceAll("\"reasoning_content\"\\s*:[^,}\\]]*[,}]?", "");

        // 1. 修复字段名后多余空格
        repaired = repaired.replaceAll("\"question\\s+\"", "\"question\"");
        repaired = repaired.replaceAll("\"options\\s+\"", "\"options\"");
        repaired = repaired.replaceAll("\"correctAnswer\\s+\"", "\"correctAnswer\"");
        repaired = repaired.replaceAll("\"analysis\\s+\"", "\"analysis\"");
        repaired = repaired.replaceAll("\"passage\\s+\"", "\"passage\"");
        repaired = repaired.replaceAll("\"questions\\s+\"", "\"questions\"");
        repaired = repaired.replaceAll("\"type\\s+\"", "\"type\"");
        repaired = repaired.replaceAll("\"score\\s+\"", "\"score\"");

        // 2. 补全缺失的括号
        int openBraces = countChar(repaired, '{');
        int closeBraces = countChar(repaired, '}');
        if (openBraces > closeBraces) {
            repaired = repaired + "}".repeat(openBraces - closeBraces);
        }

        int openBrackets = countChar(repaired, '[');
        int closeBrackets = countChar(repaired, ']');
        if (openBrackets > closeBrackets) {
            repaired = repaired + "]".repeat(openBrackets - closeBrackets);
        }

        // 3. 去除尾部多余逗号
        repaired = repaired.replaceAll(",(\\s*[}\\]])", "$1");

        return repaired;
    }

    private int countChar(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) count++;
        }
        return count;
    }

    /**
     * 确保内容是 JSON 数组格式
     */
    public String ensureArray(String content) {
        String cleaned = clean(content);
        if (cleaned.startsWith("[")) {
            return cleaned;
        }
        if (cleaned.startsWith("{")) {
            return "[" + cleaned + "]";
        }
        // 尝试提取 JSON 部分
        int start = cleaned.indexOf('[');
        if (start == -1) start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf(']');
        if (end == -1) end = cleaned.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            String extracted = cleaned.substring(start, end + 1);
            if (extracted.startsWith("{")) {
                return "[" + extracted + "]";
            }
            return extracted;
        }
        return "[]";
    }

    /**
     * 确保内容是 JSON 对象格式
     */
    public String ensureObject(String content) {
        String cleaned = clean(content);
        if (cleaned.startsWith("{")) {
            return cleaned;
        }
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        return "{}";
    }

    /**
     * 解析为题目列表
     */
    public List<QuestionDTO> parseQuestions(String content) {
        String cleaned = ensureArray(content);
        String repaired = repair(cleaned);
        try {
            return JSON.parseArray(repaired, QuestionDTO.class);
        } catch (Exception e) {
            log.error("解析题目失败，内容: {}", repaired, e);
            return new ArrayList<>();
        }
    }

    /**
     * 解析为组合题
     */
    public CompositeDTO parseComposite(String content) {
        String cleaned = ensureObject(content);
        String repaired = repair(cleaned);
        try {
            return JSON.parseObject(repaired, CompositeDTO.class);
        } catch (Exception e) {
            log.error("解析组合题失败，内容: {}", repaired, e);
            CompositeDTO fallback = new CompositeDTO();
            fallback.setPassage("");
            fallback.setQuestions(new ArrayList<>());
            return fallback;
        }
    }

    /**
     * 题目 DTO
     */
    @lombok.Data
    public static class QuestionDTO {
        private String question;
        private List<String> options;
        private Object correctAnswer;
        private String analysis;
        private Integer score;
        private String reasoningContent;
    }

    /**
     * 组合题 DTO
     */
    @lombok.Data
    public static class CompositeDTO {
        private String passage;
        private List<SubQuestionDTO> questions;
    }

    @lombok.Data
    public static class SubQuestionDTO {
        private String type;
        private String question;
        private List<String> options;
        private Object correctAnswer;
        private String analysis;
        private Integer score;
    }
}