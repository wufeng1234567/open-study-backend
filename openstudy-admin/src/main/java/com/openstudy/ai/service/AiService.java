package com.openstudy.ai.service;

import com.openstudy.ai.service.infra.AiClient;
import com.openstudy.ai.service.infra.AiClientManager;
import com.openstudy.system.domain.SysAiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiClientManager clientManager;
    private final AiConfigService aiConfigService;

    /**
     * 获取可用模型列表（显示名称）
     */
    public Map<String, String> getAvailableModels() {
        Map<String, String> models = new HashMap<>();
        models.put("zhipuai", "智谱AI (GLM-4-Plus)");
        models.put("deepseek", "DeepSeek (DeepSeek-Chat)");
        models.put("siliconflow", "硅基流动 (DeepSeek-OCR)");
        return models;
    }

    /**
     * 获取当前默认模型
     */
    public String getCurrentModel() {
        List<String> providers = clientManager.getAvailableProviders();
        return providers.isEmpty() ? "zhipuai" : providers.get(0);
    }

    /**
     * 切换模型（设置当前使用的提供商）
     */
    public boolean switchModel(String provider) {
        if (provider == null || provider.isEmpty()) {
            return false;
        }
        AiClient client = clientManager.getClient(provider);
        if (client != null && client.isAvailable()) {
            log.info("模型已切换为: {}", provider);
            return true;
        }
        log.warn("模型 {} 不可用或不存在", provider);
        return false;
    }

    /**
     * 简单聊天（使用系统默认提供商）
     */
    public String chat(String message) {
        return chat(message, null, null);
    }

    /**
     * 简单聊天（使用指定提供商）
     */
    public String chat(String message, String provider) {
        return chat(message, provider, null);
    }

    /**
     * 简单聊天（使用指定提供商和用户ID，优先用户配置）
     */
    public String chat(String message, String provider, Long userId) {
        log.info("AI聊天请求，提供商: {}, 用户ID: {}, 消息长度: {}", provider, userId, message.length());
        AiClient client = getClient(provider, userId);
        return client.chat(message);
    }

    /**
     * 带系统提示词的聊天（使用系统默认）
     */
    public String chatWithSystem(String systemPrompt, String userMessage) {
        return chatWithSystem(systemPrompt, userMessage, null, null);
    }

    /**
     * 带系统提示词的聊天（使用指定提供商）
     */
    public String chatWithSystem(String systemPrompt, String userMessage, String provider) {
        return chatWithSystem(systemPrompt, userMessage, provider, null);
    }

    /**
     * 带系统提示词的聊天（使用指定提供商和用户ID，优先用户配置）
     */
    public String chatWithSystem(String systemPrompt, String userMessage, String provider, Long userId) {
        log.info("带系统提示词的AI聊天，提供商: {}, 用户ID: {}", provider, userId);
        AiClient client = getClient(provider, userId);
        return client.chatWithSystem(systemPrompt, userMessage);
    }

    /**
     * 流式聊天（使用系统默认）
     */
    public Flux<String> chatStream(String message) {
        return chatStream(message, null, null);
    }

    /**
     * 流式聊天（使用指定提供商）
     */
    public Flux<String> chatStream(String message, String provider) {
        return chatStream(message, provider, null);
    }

    /**
     * 流式聊天（使用指定提供商和用户ID，优先用户配置）
     */
    public Flux<String> chatStream(String message, String provider, Long userId) {
        log.info("流式AI聊天，提供商: {}, 用户ID: {}", provider, userId);
        AiClient client = getClient(provider, userId);
        return client.chatStream(null, message);
    }

    /**
     * 流式聊天（带系统提示词，使用系统默认）
     */
    public Flux<String> chatStreamWithSystem(String systemPrompt, String userMessage) {
        return chatStreamWithSystem(systemPrompt, userMessage, null, null);
    }

    /**
     * 流式聊天（带系统提示词，使用指定提供商）
     */
    public Flux<String> chatStreamWithSystem(String systemPrompt, String userMessage, String provider) {
        return chatStreamWithSystem(systemPrompt, userMessage, provider, null);
    }

    /**
     * 流式聊天（带系统提示词，使用指定提供商和用户ID，优先用户配置）
     */
    public Flux<String> chatStreamWithSystem(String systemPrompt, String userMessage, String provider, Long userId) {
        log.info("流式AI聊天（带系统提示词），提供商: {}, 用户ID: {}", provider, userId);
        AiClient client = getClient(provider, userId);
        return client.chatStream(systemPrompt, userMessage);
    }

    /**
     * 根据用户ID和提供商获取客户端
     * 优先使用用户自定义配置
     */
    private AiClient getClient(String provider, Long userId) {
        // 如果指定了用户ID，优先使用用户的有效配置
        if (userId != null) {
            SysAiConfig config = aiConfigService.getEffectiveConfig(userId);
            if (config != null) {
                return aiConfigService.getClientByConfig(config);
            }
        }

        // 否则使用指定的提供商或默认
        if (provider == null || provider.isEmpty()) {
            provider = "zhipuai";
        }
        return clientManager.getClient(provider);
    }
}