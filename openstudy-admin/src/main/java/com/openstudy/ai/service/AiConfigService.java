package com.openstudy.ai.service;

import com.openstudy.ai.service.infra.AiClient;
import com.openstudy.ai.service.infra.AiClientManager;
import com.openstudy.system.domain.SysAiConfig;
import com.openstudy.system.service.ISysAiConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI 配置服务 - 统一管理用户配置和系统配置的AI调用
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AiConfigService {

    private final ISysAiConfigService sysAiConfigService;
    private final AiClientManager aiClientManager;

    /**
     * 获取用户的有效AI客户端
     *
     * @param userId 用户ID
     * @return AiClient
     */
    public AiClient getUserClient(Long userId) {
        SysAiConfig config = getEffectiveConfig(userId);
        return getClientByConfig(config);
    }

    /**
     * 获取用户的有效配置（优先用户配置 > 系统默认）
     *
     * @param userId 用户ID
     * @return AI配置
     */
    public SysAiConfig getEffectiveConfig(Long userId) {
        SysAiConfig config = sysAiConfigService.selectUserEffectiveConfig(userId);
        if (config != null) {
            log.debug("用户 {} 使用自定义配置: provider={}, model={}", userId, config.getProvider(), config.getModel());
            return config;
        }

        // 返回系统默认
        config = new SysAiConfig();
        config.setProvider("zhipuai");
        config.setProviderName("智谱AI");
        config.setModel("glm-4-plus");
        log.debug("用户 {} 使用系统默认配置: glm-4-plus", userId);
        return config;
    }

    /**
     * 根据配置获取AI客户端
     *
     * @param config AI配置
     * @return AiClient
     */
    public AiClient getClientByConfig(SysAiConfig config) {
        if (config == null) {
            // 使用系统默认
            config = new SysAiConfig();
            config.setProvider("zhipuai");
            config.setModel("glm-4-plus");
        }

        // 如果配置有自定义API Key和URL，创建动态客户端
        if (config.getUserId() != null && config.getApiKey() != null && !config.getApiKey().isEmpty()) {
            return aiClientManager.createTempClient(
                    config.getProvider(),
                    config.getApiKey(),
                    config.getBaseUrl(),
                    config.getModel(),
                    config.getThinkingMode(),
                    config.getReasoningEffort(),
                    config.getContextLength()
            );
        }

        // 否则使用系统预配置的客户端
        return aiClientManager.getClient(config.getProvider());
    }

    /**
     * 根据提供商和用户ID获取客户端
     *
     * @param provider 服务商
     * @param userId   用户ID
     * @return AiClient
     */
    public AiClient getClient(String provider, Long userId) {
        // 先尝试获取用户对该提供商的配置
        if (userId != null) {
            SysAiConfig userConfig = sysAiConfigService.selectByUserAndProvider(userId, provider);
            if (userConfig != null && userConfig.getIsEnabled() == 1
                    && userConfig.getApiKey() != null && !userConfig.getApiKey().isEmpty()) {
                return aiClientManager.createTempClient(
                        userConfig.getProvider(),
                        userConfig.getApiKey(),
                        userConfig.getBaseUrl(),
                        userConfig.getModel(),
                        userConfig.getThinkingMode(),
                        userConfig.getReasoningEffort(),
                        userConfig.getContextLength()
                );
            }
        }

        // 使用系统预配置的客户端
        return aiClientManager.getClient(provider);
    }
}
