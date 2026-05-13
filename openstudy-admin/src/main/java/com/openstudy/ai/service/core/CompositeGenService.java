package com.openstudy.ai.service.core;

import com.alibaba.fastjson.JSON;
import com.openstudy.ai.service.AiConfigService;
import com.openstudy.ai.template.PromptTemplate;
import com.openstudy.common.exception.BusinessException;
import com.openstudy.ai.parser.JsonParser;
import com.openstudy.sensitiveWord.filter.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 组合题生成服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompositeGenService {

    private final AiConfigService aiConfigService;
    private final PromptTemplate promptTemplate;
    private final JsonParser jsonParser;

    @Autowired(required = false)
    private SensitiveWordFilter sensitiveWordFilter;

    /**
     * 生成组合题
     */
    public String generate(String requirement, int questionCount, int wordCount, String provider, Long userId) {
        log.info("生成组合题，要求: {}, 题目数量: {}, 字数: {}, 用户ID: {}", requirement, questionCount, wordCount, userId);

        if (sensitiveWordFilter != null && sensitiveWordFilter.contains(requirement)) {
            log.warn("用户输入包含敏感词: {}", requirement);
            throw new BusinessException("输入内容包含敏感信息，请修改后重试");
        }

        String systemPrompt = promptTemplate.getCompositeSystemPrompt(requirement);
        String userPrompt = promptTemplate.buildCompositePrompt(requirement, wordCount, questionCount);
        log.debug("【用户提示词】: {}", userPrompt);

        String response = aiConfigService.getClient(provider, userId).chatWithSystem(
                systemPrompt,
                userPrompt
        );
        log.debug("【AI原始响应】: {}", response);

        if (sensitiveWordFilter != null && sensitiveWordFilter.contains(response)) {
            log.warn("AI返回内容包含敏感词，要求: {}", requirement);
            throw new BusinessException("AI生成内容异常，请稍后重试");
        }

        JsonParser.CompositeDTO composite = jsonParser.parseComposite(response);
        log.info("成功解析组合题，包含 {} 道小题", composite.getQuestions().size());

        return JSON.toJSONString(composite);
    }

    /**
     * 生成组合题（兼容旧接口，不带用户ID）
     */
    public String generate(String requirement, int questionCount, int wordCount, String provider) {
        return generate(requirement, questionCount, wordCount, provider, null);
    }
}