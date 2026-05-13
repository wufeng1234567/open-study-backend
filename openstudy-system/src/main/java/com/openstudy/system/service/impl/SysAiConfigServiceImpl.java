package com.openstudy.system.service.impl;

import com.openstudy.common.core.redis.RedisCache;
import com.openstudy.common.exception.ServiceException;
import com.openstudy.common.utils.StringUtils;
import com.openstudy.system.domain.SysAiConfig;
import com.openstudy.system.mapper.SysAiConfigMapper;
import com.openstudy.system.service.ISysAiConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * AI模型配置Service实现
 *
 * @author openstudy
 */
@Service
public class SysAiConfigServiceImpl implements ISysAiConfigService {

    private static final Logger log = LoggerFactory.getLogger(SysAiConfigServiceImpl.class);

    private SysAiConfigMapper sysAiConfigMapper;
    private RedisCache redisCache;

    private static final String AI_CONFIG_CACHE_KEY = "ai:config:";
    private static final int CONNECT_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 60000;

    @Autowired
    public SysAiConfigServiceImpl(SysAiConfigMapper sysAiConfigMapper, RedisCache redisCache) {
        this.sysAiConfigMapper = sysAiConfigMapper;
        this.redisCache = redisCache;
    }

    @Override
    public SysAiConfig selectSysAiConfigById(Long configId) {
        return sysAiConfigMapper.selectSysAiConfigById(configId);
    }

    @Override
    public List<SysAiConfig> selectSysAiConfigList(SysAiConfig sysAiConfig) {
        return sysAiConfigMapper.selectSysAiConfigList(sysAiConfig);
    }

    @Override
    public List<SysAiConfig> selectByUserId(Long userId) {
        return sysAiConfigMapper.selectByUserId(userId);
    }

    @Override
    public SysAiConfig selectUserEffectiveConfig(Long userId) {
        // 1. 先查用户自己的配置
        SysAiConfig userConfig = sysAiConfigMapper.selectDefaultByUserId(userId);
        if (userConfig != null && userConfig.getIsEnabled() == 1) {
            return userConfig;
        }

        // 2. 查系统默认配置
        SysAiConfig systemConfig = sysAiConfigMapper.selectSystemDefault();
        if (systemConfig != null && systemConfig.getIsEnabled() == 1) {
            return systemConfig;
        }

        // 3. 如果都没有，返回null
        return null;
    }

    @Override
    public SysAiConfig selectByUserAndProvider(Long userId, String provider) {
        return sysAiConfigMapper.selectByUserAndProvider(userId, provider);
    }

    @Override
    @Transactional
    public int insertSysAiConfig(SysAiConfig sysAiConfig) {
        // 如果设置为默认，先取消其他默认
        if (sysAiConfig.getIsDefault() != null && sysAiConfig.getIsDefault() == 1) {
            sysAiConfigMapper.clearUserDefault(sysAiConfig.getUserId());
        }

        int result = sysAiConfigMapper.insertSysAiConfig(sysAiConfig);

        // 清除缓存
        if (sysAiConfig.getUserId() != null) {
            redisCache.deleteObject(AI_CONFIG_CACHE_KEY + sysAiConfig.getUserId());
        }

        return result;
    }

    @Override
    @Transactional
    public int updateSysAiConfig(SysAiConfig sysAiConfig) {
        // 如果设置为默认，先取消其他默认
        if (sysAiConfig.getIsDefault() != null && sysAiConfig.getIsDefault() == 1) {
            sysAiConfigMapper.clearUserDefault(sysAiConfig.getUserId());
        }

        int result = sysAiConfigMapper.updateSysAiConfig(sysAiConfig);

        // 清除缓存
        if (sysAiConfig.getUserId() != null) {
            redisCache.deleteObject(AI_CONFIG_CACHE_KEY + sysAiConfig.getUserId());
        }

        return result;
    }

    @Override
    @Transactional
    public int deleteSysAiConfigById(Long configId) {
        SysAiConfig config = sysAiConfigMapper.selectSysAiConfigById(configId);
        if (config == null) {
            throw new ServiceException("配置不存在");
        }

        int result = sysAiConfigMapper.deleteSysAiConfigById(configId);

        // 清除缓存
        if (config.getUserId() != null) {
            redisCache.deleteObject(AI_CONFIG_CACHE_KEY + config.getUserId());
        }

        return result;
    }

    @Override
    @Transactional
    public int deleteSysAiConfigByIds(Long[] configIds) {
        for (Long configId : configIds) {
            deleteSysAiConfigById(configId);
        }
        return configIds.length;
    }

    @Override
    @Transactional
    public boolean setAsDefault(Long configId, Long userId) {
        SysAiConfig config = sysAiConfigMapper.selectSysAiConfigById(configId);
        if (config == null) {
            throw new ServiceException("配置不存在");
        }

        // 验证是否是该用户的配置
        if (!userId.equals(config.getUserId())) {
            throw new ServiceException("无权操作此配置");
        }

        // 取消其他默认
        sysAiConfigMapper.clearUserDefault(userId);

        // 设置当前为默认
        config.setIsDefault(1);
        int result = sysAiConfigMapper.updateSysAiConfig(config);

        // 清除缓存
        redisCache.deleteObject(AI_CONFIG_CACHE_KEY + userId);

        return result > 0;
    }

    @Override
    public String testConnection(SysAiConfig sysAiConfig) {
        if (sysAiConfig == null) {
            return "配置信息不完整";
        }

        if (StringUtils.isEmpty(sysAiConfig.getApiKey())) {
            return "API Key不能为空";
        }

        if (StringUtils.isEmpty(sysAiConfig.getModel())) {
            return "模型名称不能为空";
        }

        try {
            String response = testAiConnection(
                    sysAiConfig.getProvider(),
                    sysAiConfig.getApiKey(),
                    sysAiConfig.getBaseUrl(),
                    sysAiConfig.getModel());

            if (response == null) {
                return null;
            } else if (response.startsWith("ERROR:")) {
                return response.substring(7);
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("测试连接失败: {}", e.getMessage(), e);
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("401")) {
                return "API Key无效或已过期";
            } else if (errorMsg != null && errorMsg.contains("403")) {
                return "API Key无权限访问此模型";
            } else if (errorMsg != null && errorMsg.contains("404")) {
                return "模型不存在，请检查模型名称";
            } else if (errorMsg != null && errorMsg.contains("429")) {
                return "请求频率超限，请稍后重试";
            } else if (errorMsg != null && errorMsg.contains("timeout")) {
                return "请求超时，请检查网络或API地址";
            } else if (errorMsg != null && errorMsg.contains("connection")) {
                return "无法连接到API服务器，请检查API地址";
            }
            return "连接失败: " + errorMsg;
        }
    }

    private String testAiConnection(String provider, String apiKey, String baseUrl, String model) throws Exception {
        log.info("========== [API连接测试调试信息] ==========");
        log.info("Provider: {}", provider);
        log.info("API Key: [{}]", apiKey);
        log.info("API Key 长度: {}", apiKey != null ? apiKey.length() : 0);
        log.info("Base URL: [{}]", baseUrl);
        log.info("Model: [{}]", model);
        log.info("==========================================");

        String requestBody;
        String apiUrl;

        if ("zhipuai".equalsIgnoreCase(provider)) {
            apiUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
            requestBody = String.format(
                    "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}],\"max_tokens\":10}",
                    model != null && !model.isEmpty() ? model : "glm-4-plus");
        } else if ("deepseek".equalsIgnoreCase(provider)) {
            apiUrl = (baseUrl != null && !baseUrl.isEmpty()) ? baseUrl + "/v1/chat/completions"
                    : "https://api.deepseek.com/v1/chat/completions";
            requestBody = String.format(
                    "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}],\"max_tokens\":10}",
                    model != null && !model.isEmpty() ? model : "deepseek-chat");
        } else if ("siliconflow".equalsIgnoreCase(provider)) {
            apiUrl = (baseUrl != null && !baseUrl.isEmpty()) ? baseUrl + "/v1/chat/completions"
                    : "https://api.siliconflow.cn/v1/chat/completions";
            requestBody = String.format(
                    "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}],\"max_tokens\":10}",
                    model != null && !model.isEmpty() ? model : "Qwen/Qwen2.5-7B-Instruct");
        } else if ("openai".equalsIgnoreCase(provider)) {
            apiUrl = (baseUrl != null && !baseUrl.isEmpty()) ? baseUrl + "/v1/chat/completions"
                    : "https://api.openai.com/v1/chat/completions";
            requestBody = String.format(
                    "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}],\"max_tokens\":10}",
                    model != null && !model.isEmpty() ? model : "gpt-3.5-turbo");
        } else {
            return "ERROR:不支持的服务商: " + provider;
        }

        log.info("最终请求URL: {}", apiUrl);
        log.info("发送的 Authorization Header: Bearer [{}]", apiKey);

        return sendHttpRequest(apiUrl, apiKey, requestBody, provider);
    }

    private String sendHttpRequest(String apiUrl, String apiKey, String requestBody, String provider) throws Exception {
        HttpURLConnection conn = null;
        try {
            log.info(">>> [HTTP请求开始]");
            log.info("URL: {}", apiUrl);
            log.info("Method: POST");
            log.info("Authorization: Bearer [{}]", apiKey);
            log.info("Request Body: {}", requestBody);

            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            log.info("<<< [HTTP响应码]: {}", responseCode);

            StringBuilder response = new StringBuilder();
            InputStream inputStream = responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
            if (inputStream != null) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }
            }
            log.info("响应内容: [{}]", response.toString());

            if (responseCode == 200) {
                return null;
            } else if (responseCode == 401) {
                return "ERROR:API Key无效或已过期";
            } else if (responseCode == 403) {
                return "ERROR:API Key无权限访问此模型";
            } else if (responseCode == 404) {
                return "ERROR:模型不存在，请检查模型名称";
            } else if (responseCode == 429) {
                return "ERROR:请求频率超限，请稍后重试";
            } else {
                return "ERROR:HTTP " + responseCode + ": " + response.toString();
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
