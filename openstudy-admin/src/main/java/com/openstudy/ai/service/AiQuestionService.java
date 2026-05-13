package com.openstudy.ai.service;

import com.openstudy.ai.template.PromptTemplate;
import com.openstudy.ai.service.core.CompositeGenService;
import com.openstudy.ai.service.core.QuestionGenService;
import com.openstudy.sensitiveWord.filter.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * AI 出题服务（精简版，委托给专门的服务）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiQuestionService {

    private final QuestionGenService questionGenService;
    private final CompositeGenService compositeGenService;
    private final AiService aiService;

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    // ==================== 非流式生成 ====================

    /**
     * 生成普通题目
     */
    public String generateQuestions(String knowledgePoint, String questionType, Integer count, String provider, Long userId) {
        return questionGenService.generate(knowledgePoint, questionType, count, provider, userId);
    }

    /**
     * 生成普通题目（兼容旧接口）
     */
    public String generateQuestions(String knowledgePoint, String questionType, Integer count, String provider) {
        return questionGenService.generate(knowledgePoint, questionType, count, provider, null);
    }

    /**
     * 生成组合题
     */
    public String generateCompositeQuestion(String requirement, Integer questionCount, Integer wordCount, String provider, Long userId) {
        return compositeGenService.generate(requirement, questionCount, wordCount, provider, userId);
    }

    /**
     * 生成组合题（兼容旧接口）
     */
    public String generateCompositeQuestion(String requirement, Integer questionCount, Integer wordCount, String provider) {
        return compositeGenService.generate(requirement, questionCount, wordCount, provider, null);
    }

    // ==================== 流式生成（保留） ====================

    /**
     * 流式生成阅读理解题（组合题）
     */
    public Flux<String> generateReadingComprehensionStream(String requirement, Integer questionCount, String provider) {
        log.info("流式生成组合题，要求: {}, 题目数量: {}", requirement, questionCount);
        // 这里可以后续用 CompositeGenService 的流式版本
        return aiService.chatStreamWithSystem(
                PromptTemplate.COMPOSITE_SYSTEM_PROMPT,
                requirement,
                provider
        );
    }

    // ==================== 其他保留方法 ====================

    public String generateReadingComprehension(String passage, Integer questionCount, String provider) {
        log.info("生成阅读理解题，文章长度: {}, 题目数量: {}", passage.length(), questionCount);
        return generateCompositeQuestion(passage, questionCount, 300, provider);
    }

    public String analyzeAnswer(String question, String userAnswer, String correctAnswer, String provider) {
        log.info("解析答案，题目: {}, 用户答案: {}, 正确答案: {}", question, userAnswer, correctAnswer);
        // 暂时返回空实现
        return "{}";
    }

    public String generateExamPaper(String subject, String difficulty, Integer totalScore, String provider) {
        log.info("生成试卷，科目: {}, 难度: {}, 总分: {}", subject, difficulty, totalScore);
        // 暂时返回空实现
        return "{}";
    }

    public String getSystemPrompt() {
        return PromptTemplate.SYSTEM_PROMPT;
    }

    public String buildUserPrompt(String knowledgePoint, String questionType, Integer count) {
        return new PromptTemplate().buildQuestionPrompt(knowledgePoint, questionType, count);
    }
}