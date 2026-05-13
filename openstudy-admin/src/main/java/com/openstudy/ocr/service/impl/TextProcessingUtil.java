package com.openstudy.ocr.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本处理工具类 - 专门处理音标删除
 */
public class TextProcessingUtil {

    // 匹配音标的正则表达式（两个斜杠之间的内容）
    private static final Pattern PHONETIC_PATTERN = Pattern.compile("/[^/]+/");

    // 匹配括号音标：如 [pəˈfɔ:məns]
    private static final Pattern BRACKET_PHONETIC_PATTERN = Pattern.compile("\\[[^\\]]+\\]");

    // 匹配常见英文单词
    private static final Pattern ENGLISH_WORD_PATTERN = Pattern.compile("^[a-zA-Z]+(?:['-][a-zA-Z]+)*$");

    // 匹配中文
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\u4e00-\u9fa5]+");

    /**
     * 删除文本中的音标部分（两个斜杠之间的内容）
     */
    public static String removePhonetic(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;

        // 1. 删除 /音标/ 格式
        result = PHONETIC_PATTERN.matcher(result).replaceAll("");

        // 2. 删除 [音标] 格式
        result = BRACKET_PHONETIC_PATTERN.matcher(result).replaceAll("");

        // 3. 删除可能的残留音标字符
        result = result.replaceAll("[ˈˌːˑ]", "");

        return result.trim();
    }

    /**
     * 提取所有音标部分
     */
    public static List<String> extractPhonetics(String text) {
        List<String> phonetics = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return phonetics;
        }

        // 提取 /音标/
        Matcher slashMatcher = PHONETIC_PATTERN.matcher(text);
        while (slashMatcher.find()) {
            phonetics.add(slashMatcher.group());
        }

        // 提取 [音标]
        Matcher bracketMatcher = BRACKET_PHONETIC_PATTERN.matcher(text);
        while (bracketMatcher.find()) {
            phonetics.add(bracketMatcher.group());
        }

        return phonetics;
    }

    /**
     * 判断是否为英文单词
     */
    public static boolean isEnglishWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String cleaned = text.trim();

        // 先移除可能的音标
        cleaned = removePhonetic(cleaned);

        // 检查是否符合英文单词模式
        return ENGLISH_WORD_PATTERN.matcher(cleaned).matches() &&
                cleaned.length() >= 2 &&
                cleaned.length() <= 20;
    }

    /**
     * 判断是否包含中文
     */
    public static boolean containsChinese(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return CHINESE_PATTERN.matcher(text).find();
    }

    /**
     * 清理一行文本
     */
    public static String cleanLine(String line) {
        if (line == null || line.isEmpty()) {
            return line;
        }

        // 1. 删除音标
        line = removePhonetic(line);

        // 2. 移除多余空格
        line = line.replaceAll("\\s+", " ").trim();

        // 3. 清理特殊字符
        line = line.replaceAll("[*•●○■□▲△▶◀◆◇★☆♀♂♣♦♠♥♪♫♬♭♮♯]", "");

        return line;
    }

    /**
     * 提取中文部分（包含括号）
     */
    public static String extractChineseWithContext(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        StringBuilder chinese = new StringBuilder();

        // 提取包含中文的上下文（包括括号等）
        Pattern pattern = Pattern.compile("[\\(（][^)\\)]*[\u4e00-\u9fa5]+[^)\\)]*[\\)）]|[\u4e00-\u9fa5]+");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            if (chinese.length() > 0) {
                chinese.append(" ");
            }
            chinese.append(matcher.group().trim());
        }

        return chinese.toString().trim();
    }

    /**
     * 提取英文单词部分
     */
    public static String extractEnglishWords(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // 先删除音标
        String cleaned = removePhonetic(text);

        // 提取连续的字母序列（允许短横线和撇号）
        Pattern pattern = Pattern.compile("[a-zA-Z]+(?:['-][a-zA-Z]+)*");
        Matcher matcher = pattern.matcher(cleaned);

        StringBuilder english = new StringBuilder();
        while (matcher.find()) {
            String word = matcher.group();
            // 过滤掉太短的或无意义的组合
            if (word.length() >= 2 && !isLikelyPhoneticRemnant(word)) {
                if (english.length() > 0) {
                    english.append(" ");
                }
                english.append(word);
            }
        }

        return english.toString().trim();
    }

    /**
     * 判断是否为音标残留
     */
    private static boolean isLikelyPhoneticRemnant(String text) {
        if (text == null || text.length() < 2) {
            return false;
        }

        // 检查是否为奇怪的组合
        String lower = text.toLowerCase();

        // 包含音标特征字符
        if (lower.matches(".*[`':].*")) {
            return true;
        }

        // 元音比例异常
        long vowelCount = lower.chars().filter(c -> "aeiou".indexOf(c) != -1).count();
        double vowelRatio = (double) vowelCount / lower.length();
        if (vowelRatio < 0.1 || vowelRatio > 0.8) {
            return true;
        }

        return false;
    }
}