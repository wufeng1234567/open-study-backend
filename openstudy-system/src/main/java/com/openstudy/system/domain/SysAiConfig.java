package com.openstudy.system.domain;

import com.openstudy.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * AI模型配置表 sys_ai_config
 *
 * @author openstudy
 */
public class SysAiConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long configId;
    private Long userId;
    private String provider;
    private String providerName;
    private String model;
    private String apiKey;
    private String baseUrl;
    private Integer isDefault;
    private Integer isEnabled;
    private String status;
    private String thinkingMode;
    private String reasoningEffort;
    private String contextLength;

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Integer getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }

    public Integer getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Integer isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThinkingMode() {
        return thinkingMode;
    }

    public void setThinkingMode(String thinkingMode) {
        this.thinkingMode = thinkingMode;
    }

    public String getReasoningEffort() {
        return reasoningEffort;
    }

    public void setReasoningEffort(String reasoningEffort) {
        this.reasoningEffort = reasoningEffort;
    }

    public String getContextLength() {
        return contextLength;
    }

    public void setContextLength(String contextLength) {
        this.contextLength = contextLength;
    }
}