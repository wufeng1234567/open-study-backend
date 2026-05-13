package com.openstudy.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 多模型配置类
 * 支持智谱AI、Deepseek、OpenAI 一键切换
 *
 * @author openstudy
 */
@Slf4j
@Configuration
public class AiConfig {

    // ==================== 智谱AI配置 ====================

    @Bean
    @ConditionalOnProperty(prefix = "spring.ai.zhipuai", name = "api-key")
    public ZhiPuAiChatModel zhiPuAiChatModel(
            @Value("${spring.ai.zhipuai.api-key}") String apiKey,
            @Value("${spring.ai.zhipuai.base-url:https://open.bigmodel.cn/api/paas/v4}") String baseUrl,
            @Value("${spring.ai.zhipuai.chat.options.model:glm-4-plus}") String model,
            @Value("${spring.ai.zhipuai.chat.options.temperature:0.7}") Double temperature,
            @Value("${spring.ai.zhipuai.chat.options.max-tokens:2048}") Integer maxTokens) {

        log.info("初始化智谱AI模型，model: {}, baseUrl: {}", model, baseUrl);

        // 修正：使用 setter 方式构建选项
        ZhiPuAiChatOptions options = ZhiPuAiChatOptions.builder()
                .withModel(model)
                .withTemperature((double) temperature.floatValue())
                .withMaxTokens(maxTokens)
                .build();

        // 创建 API 实例
        ZhiPuAiApi zhiPuAiApi = new ZhiPuAiApi(baseUrl, apiKey);

        return new ZhiPuAiChatModel(zhiPuAiApi, options);
    }

    // ==================== Deepseek配置（兼容OpenAI接口） ====================

    @Bean
    @ConditionalOnProperty(prefix = "spring.ai.openai", name = "api-key")
    public OpenAiChatModel deepseekChatModel(
            @Value("${spring.ai.openai.api-key}") String apiKey,
            @Value("${spring.ai.openai.base-url:https://api.deepseek.com/v1}") String baseUrl,
            @Value("${spring.ai.openai.chat.options.model:deepseek-chat}") String model,
            @Value("${spring.ai.openai.chat.options.temperature:0.7}") Double temperature,
            @Value("${spring.ai.openai.chat.options.max-tokens:2048}") Integer maxTokens) {

        log.info("初始化Deepseek模型，model: {}, baseUrl: {}", model, baseUrl);

        // 修正：使用 setter 方式构建选项
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(model)
                .withTemperature(temperature)
                .withMaxTokens(maxTokens)
                .build();

        // 创建 API 实例
        OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);

        return new OpenAiChatModel(openAiApi, options);
    }


    // ==================== 硅基流动配置（兼容OpenAI接口） ====================

    @Bean
    @ConditionalOnProperty(prefix = "spring.ai.siliconflow", name = "api-key")
    public OpenAiChatModel siliconflowChatModel(
            @Value("${spring.ai.siliconflow.api-key}") String apiKey,
            @Value("${spring.ai.siliconflow.base-url:https://api.siliconflow.cn}") String baseUrl,
            @Value("${spring.ai.siliconflow.chat.options.model:deepseek-ai/DeepSeek-OCR}") String model,
            @Value("${spring.ai.siliconflow.chat.options.temperature:0.1}") Double temperature,
            @Value("${spring.ai.siliconflow.chat.options.max-tokens:4096}") Integer maxTokens) {

        log.info("初始化硅基流动模型，model: {}, baseUrl: {}", model, baseUrl);

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(model)
                .withTemperature(temperature)
                .withMaxTokens(maxTokens)
                .build();

        // 硅基流动的 baseUrl 需要拼接 /v1
        // String fullBaseUrl = baseUrl + "/v1";
        // OpenAiApi openAiApi = new OpenAiApi(fullBaseUrl, apiKey);
        // 硅基流动的 baseUrl 直接使用配置的地址
        OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);

        return new OpenAiChatModel(openAiApi, options);
    }

    // ==================== ChatClient 配置 ====================

    /**
     * 默认的 ChatClient（根据配置自动选择提供商）
     */
    @Bean
    @Primary
    public ChatClient defaultChatClient(
            @Value("${ai.default-provider:zhipuai}") String defaultProvider,
            @Qualifier("zhiPuAiChatModel") ZhiPuAiChatModel zhiPuModel,
            @Qualifier("deepseekChatModel") OpenAiChatModel deepseekModel) {

        ChatModel selectedModel = selectModel(defaultProvider, zhiPuModel, deepseekModel);
        log.info("默认AI模型提供商: {}", defaultProvider);
        return ChatClient.builder(selectedModel).build();
    }

    /**
     * 智谱AI专用 ChatClient
     */
    @Bean
    public ChatClient zhiPuChatClient(@Qualifier("zhiPuAiChatModel") ZhiPuAiChatModel model) {
        log.info("初始化智谱AI ChatClient");
        return ChatClient.builder(model).build();
    }

    /**
     * Deepseek专用 ChatClient
     */
    @Bean
    public ChatClient deepseekChatClient(@Qualifier("deepseekChatModel") OpenAiChatModel model) {
        log.info("初始化Deepseek ChatClient");
        return ChatClient.builder(model).build();
    }

    /**
     * 模型选择器（支持动态切换）
     */
    private ChatModel selectModel(String provider, ZhiPuAiChatModel zhiPuModel, OpenAiChatModel deepseekModel) {
        switch (provider.toLowerCase()) {
            case "deepseek":
                return deepseekModel;
            case "zhipuai":
            default:
                return zhiPuModel;
        }
    }

    /**
     * 模型工厂（用于运行时动态切换）
     */
    @Bean
    public AiModelFactory aiModelFactory(
            @Qualifier("zhiPuAiChatModel") ZhiPuAiChatModel zhiPuModel,
            @Qualifier("deepseekChatModel") OpenAiChatModel deepseekModel,
            @Qualifier("siliconflowChatModel") OpenAiChatModel siliconflowModel) {

        Map<String, ChatModel> modelMap = new ConcurrentHashMap<>();
        modelMap.put("zhipuai", zhiPuModel);
        modelMap.put("deepseek", deepseekModel);
        modelMap.put("siliconflow", siliconflowModel);

        return new AiModelFactory(modelMap);
    }
}