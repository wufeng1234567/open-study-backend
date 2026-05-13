package com.openstudy.ai.service.infra;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 客户端管理器
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AiClientManager {

    private final Map<String, AiClient> clientMap = new ConcurrentHashMap<>();
    private final List<AiClient> clients;

    @PostConstruct
    public void init() {
        for (AiClient client : clients) {
            if (client == null || client.getProviderName() == null || client.getProviderName().isEmpty()) {
                log.warn("跳过无效的 AI 客户端: {}", client);
                continue;
            }
            clientMap.put(client.getProviderName(), client);
            log.info("注册 AI 客户端: {}", client.getProviderName());
        }
    }

    public AiClient getClient(String provider) {
        if (provider == null || provider.isEmpty()) {
            provider = "zhipuai";
        }
        AiClient client = clientMap.get(provider.toLowerCase());
        if (client == null || !client.isAvailable()) {
            log.warn("客户端 {} 不可用，降级到 zhipuai", provider);
            return clientMap.get("zhipuai");
        }
        return client;
    }

    public List<String> getAvailableProviders() {
        return clientMap.values().stream()
                .filter(AiClient::isAvailable)
                .map(AiClient::getProviderName)
                .toList();
    }

    /**
     * 创建临时客户端（用于测试连接或动态配置）
     *
     * @param provider 提供商名称
     * @param apiKey   API Key
     * @param baseUrl  API 地址（可选，使用默认地址）
     * @param model    模型名称
     * @return 动态客户端
     */
    public AiClient createTempClient(String provider, String apiKey, String baseUrl, String model) {
        if (provider == null || apiKey == null || model == null) {
            return null;
        }

        DynamicAiClient client = new DynamicAiClient(provider, apiKey, baseUrl, model);
        log.info("创建临时AI客户端: provider={}, model={}", provider, model);
        return client;
    }

    public AiClient createTempClient(String provider, String apiKey, String baseUrl, String model,
                                      String thinkingMode, String reasoningEffort, String contextLength) {
        if (provider == null || apiKey == null || model == null) {
            return null;
        }

        DynamicAiClient client = new DynamicAiClient(provider, apiKey, baseUrl, model,
                thinkingMode, reasoningEffort, contextLength);
        log.info("创建临时AI客户端: provider={}, model={}, thinking={}, reasoning={}, context={}",
                provider, model, thinkingMode, reasoningEffort, contextLength);
        return client;
    }
}