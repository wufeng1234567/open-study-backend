package com.openstudy.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;

import java.util.Map;

/**
 * AI 模型工厂
 * 用于运行时动态切换AI模型
 * 
 * @author openstudy
 */
@Slf4j
public class AiModelFactory {
    
    private final Map<String, ChatModel> modelMap;
    private String currentProvider = "zhipuai";
    
    public AiModelFactory(Map<String, ChatModel> modelMap) {
        this.modelMap = modelMap;
        log.info("AI模型工厂初始化，可用模型: {}", modelMap.keySet());
    }
    
    /**
     * 获取当前使用的模型
     */
    public ChatModel getCurrentModel() {
        ChatModel model = modelMap.get(currentProvider);
        if (model == null) {
            log.warn("模型 {} 不存在，使用第一个可用模型", currentProvider);
            currentProvider = modelMap.keySet().iterator().next();
            model = modelMap.get(currentProvider);
        }
        return model;
    }
    
    /**
     * 切换模型
     */
    public boolean switchModel(String provider) {
        if (modelMap.containsKey(provider)) {
            this.currentProvider = provider;
            log.info("AI模型已切换为: {}", provider);
            return true;
        }
        log.warn("切换模型失败，模型 {} 不存在", provider);
        return false;
    }
    
    /**
     * 获取当前模型名称
     */
    public String getCurrentProvider() {
        return currentProvider;
    }
    
    /**
     * 获取所有可用模型
     */
    public Map<String, ChatModel> getAllModels() {
        return modelMap;
    }
}