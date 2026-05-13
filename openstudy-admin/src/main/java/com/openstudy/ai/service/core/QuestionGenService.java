package com.openstudy.ai.service.core;

import com.alibaba.fastjson.JSON;
import com.openstudy.ai.parser.JsonParser;
import com.openstudy.ai.service.AiConfigService;
import com.openstudy.ai.template.PromptTemplate;
import com.openstudy.common.exception.BusinessException;
import com.openstudy.sensitiveWord.filter.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 普通题目生成服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionGenService {

    private final AiConfigService aiConfigService;
    private final PromptTemplate promptTemplate;
    private final JsonParser jsonParser;

    @Autowired(required = false)
    private SensitiveWordFilter sensitiveWordFilter;

    /**
     * 生成普通题目
     */
    public String generate(String knowledgePoint, String questionType, int count, String provider, Long userId) {
        log.info("生成普通题目，知识点: {}, 题型: {}, 数量: {}, 用户ID: {}", knowledgePoint, questionType, count, userId);

        if (sensitiveWordFilter != null && sensitiveWordFilter.contains(knowledgePoint)) {
            log.warn("用户输入包含敏感词: {}", knowledgePoint);
            throw new BusinessException("输入内容包含敏感信息，请修改后重试");
        }

        String userPrompt = promptTemplate.buildQuestionPrompt(knowledgePoint, questionType, count);
        log.debug("【用户提示词】: {}", userPrompt);

        String response = aiConfigService.getClient(provider, userId).chatWithSystem(
                PromptTemplate.SYSTEM_PROMPT,
                userPrompt
        );
        log.debug("【AI原始响应】: {}", response);

        if (sensitiveWordFilter != null && sensitiveWordFilter.contains(response)) {
            log.warn("AI返回内容包含敏感词，输入: {}", knowledgePoint);
            throw new BusinessException("AI生成内容异常，请稍后重试");
        }

        List<JsonParser.QuestionDTO> questions = jsonParser.parseQuestions(response);
        log.info("成功解析 {} 道题目", questions.size());

        return JSON.toJSONString(questions);
    }

    /**
     * 生成普通题目（兼容旧接口，不带用户ID）
     */
    public String generate(String knowledgePoint, String questionType, int count, String provider) {
        return generate(knowledgePoint, questionType, count, provider, null);
    }
}