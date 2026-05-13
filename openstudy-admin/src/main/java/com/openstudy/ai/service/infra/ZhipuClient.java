package com.openstudy.ai.service.infra;

import com.openstudy.framework.ai.ZhipuAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 智谱 AI 客户端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ZhipuClient implements AiClient {

    private final ZhipuAiClient zhipuAiClient;

    @Override
    public String chat(String message) {
        return zhipuAiClient.chat(message);
    }

    @Override
    public String chatWithSystem(String systemPrompt, String userMessage) {
        return zhipuAiClient.chatWithSystem(systemPrompt, userMessage, "glm-4-plus");
    }

    @Override
    public Flux<String> chatStream(String systemPrompt, String userMessage) {
        return zhipuAiClient.chatStream(systemPrompt, userMessage, "glm-4-plus");
    }

    @Override
    public String getProviderName() {
        return "zhipuai";
    }
}