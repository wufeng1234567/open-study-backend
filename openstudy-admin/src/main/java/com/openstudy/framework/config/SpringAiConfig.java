package com.openstudy.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SpringAiConfig {

    @Value("${spring.ai.zhipuai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.zhipuai.base-url:https://open.bigmodel.cn}")
    private String baseUrl;

    @Value("${spring.ai.zhipuai.chat.options.model:glm-4-plus}")
    private String model;

    @Value("${spring.ai.zhipuai.chat.options.temperature:0.7}")
    private Double temperature;

    @Value("${spring.ai.zhipuai.chat.options.max-tokens:2048}")
    private Integer maxTokens;

    /**
     * 创建智谱AI ChatModel（测试用，改名避免冲突）
     */
    @Bean(name = "testZhiPuAiChatModel")  // ← 改个名字
    @ConditionalOnProperty(prefix = "spring.ai.zhipuai", name = "api-key")
    public ZhiPuAiChatModel testZhiPuAiChatModel() {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("智谱AI api-key 未配置，跳过初始化");
            return null;
        }

        log.info("初始化 Spring AI 智谱AI模型（测试），model: {}, baseUrl: {}", model, baseUrl);

        ZhiPuAiApi zhiPuAiApi = new ZhiPuAiApi(baseUrl, apiKey);

        ZhiPuAiChatOptions options = ZhiPuAiChatOptions.builder()
                .withModel(model)
                .withTemperature(temperature)
                .withMaxTokens(maxTokens)
                .build();

        return new ZhiPuAiChatModel(zhiPuAiApi, options);
    }

    /**
     * 创建 ChatClient
     */
    @Bean
    @ConditionalOnProperty(prefix = "spring.ai.zhipuai", name = "api-key")
    public ChatClient springAiChatClient(ZhiPuAiChatModel testZhiPuAiChatModel) {
        if (testZhiPuAiChatModel == null) {
            log.warn("testZhiPuAiChatModel 为 null，跳过 ChatClient 初始化");
            return null;
        }
        log.info("初始化 Spring AI ChatClient");
        return ChatClient.builder(testZhiPuAiChatModel).build();
    }
}