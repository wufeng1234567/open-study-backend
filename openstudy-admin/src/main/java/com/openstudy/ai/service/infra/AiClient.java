package com.openstudy.ai.service.infra;

import reactor.core.publisher.Flux;

/**
 * AI 客户端接口
 */
public interface AiClient {

    /**
     * 简单聊天
     */
    String chat(String message);

    /**
     * 带系统提示词的聊天
     */
    String chatWithSystem(String systemPrompt, String userMessage);

    /**
     * 流式聊天
     */
    Flux<String> chatStream(String systemPrompt, String userMessage);

    /**
     * 获取提供商名称
     */
    String getProviderName();

    /**
     * 是否可用
     */
    default boolean isAvailable() {
        return true;
    }
}