package com.openstudy.ai.service.infra;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 动态AI客户端 - 支持运行时传入API Key、Base URL、Model
 * 用于用户自定义配置和测试连接
 */
@Slf4j
@Component
public class DynamicAiClient implements AiClient {

    private String provider;
    private String apiKey;
    private String baseUrl;
    private String model;
    private String thinkingMode;
    private String reasoningEffort;
    private String contextLength;

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public DynamicAiClient() {
    }

    public DynamicAiClient(String provider, String apiKey, String baseUrl, String model) {
        this.provider = provider;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
    }

    public DynamicAiClient(String provider, String apiKey, String baseUrl, String model,
                           String thinkingMode, String reasoningEffort, String contextLength) {
        this.provider = provider;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.thinkingMode = thinkingMode;
        this.reasoningEffort = reasoningEffort;
        this.contextLength = contextLength;
    }

    @Override
    public String chat(String message) {
        return chatWithSystem(null, message);
    }

    @Override
    public String chatWithSystem(String systemPrompt, String userMessage) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();

            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                Map<String, String> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
                messages.add(systemMsg);
            }

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            return doRequest(messages);
        } catch (Exception e) {
            log.error("DynamicAiClient 调用失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Flux<String> chatStream(String systemPrompt, String userMessage) {
        return Flux.create(sink -> {
            executor.submit(() -> {
                try {
                    doStreamRequest(systemPrompt, userMessage, sink);
                } catch (Exception e) {
                    log.error("DynamicAiClient 流式调用失败: {}", e.getMessage(), e);
                    sink.error(e);
                }
            });
        });
    }

    @Override
    public String getProviderName() {
        return provider;
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty() && model != null && !model.isEmpty();
    }

    public void setConfig(String provider, String apiKey, String baseUrl, String model) {
        this.provider = provider;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
    }

    private String doRequest(List<Map<String, String>> messages) throws Exception {
        String apiUrl = getApiUrl();
        log.info("DynamicAiClient 请求，URL: {}, Provider: {}, Model: {}", apiUrl, provider, model);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 8192);
        requestBody.put("stream", false);

        if ("deepseek".equalsIgnoreCase(provider)) {
            JSONObject extraBody = new JSONObject();
            if (thinkingMode != null && !thinkingMode.isEmpty() && !"auto".equals(thinkingMode)) {
                JSONObject thinking = new JSONObject();
                thinking.put("type", thinkingMode);
                extraBody.put("thinking", thinking);
            }
            if (reasoningEffort != null && !reasoningEffort.isEmpty()) {
                requestBody.put("reasoning_effort", reasoningEffort);
            }
            if (contextLength != null && !contextLength.isEmpty()) {
                requestBody.put("max_tokens", parseContextLength(contextLength));
            }
            if (!extraBody.isEmpty()) {
                requestBody.put("extra_body", extraBody);
            }
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(120000);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toJSONString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        log.info("DynamicAiClient 响应码: {}", responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                String rawResponse = response.toString();
                log.error("【DEBUG】doRequest 原始响应: {}", rawResponse);
                return parseResponse(rawResponse);
            }
        } else {
            String errorResponse = readErrorStream(connection);
            log.error("DynamicAiClient 请求失败，响应码: {}, 错误: {}", responseCode, errorResponse);
            throw new RuntimeException("AI服务响应异常: " + errorResponse);
        }
    }

    private void doStreamRequest(String systemPrompt, String userMessage, FluxSink<String> sink) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                Map<String, String> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
                messages.add(systemMsg);
            }
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            String apiUrl = getApiUrl();
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2048);
            requestBody.put("stream", true);

            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(120000);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toJSONString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6).trim();
                            if ("[DONE]".equals(data)) {
                                break;
                            }
                            String content = extractContentFromStreamData(data);
                            if (content != null && !content.isEmpty()) {
                                sink.next(content);
                            }
                        }
                    }
                }
                sink.complete();
            } else {
                String errorResponse = readErrorStream(connection);
                sink.error(new RuntimeException("AI服务响应异常: " + errorResponse));
            }
        } catch (Exception e) {
            log.error("DynamicAiClient 流式调用失败", e);
            sink.error(e);
        }
    }

    private String getApiUrl() {
        if (baseUrl != null && !baseUrl.isEmpty()) {
            String url = baseUrl.trim();
            if (!url.endsWith("/")) {
                url += "/";
            }
            // OpenAI 兼容格式
            if (provider != null && provider.equals("zhipuai")) {
                return url + "v4/chat/completions";
            } else if (provider != null && provider.equals("deepseek")) {
                return url + "chat/completions";
            } else if (provider != null && provider.equals("siliconflow")) {
                return url + "v1/chat/completions";
            } else if (provider != null && provider.equals("openai")) {
                return url + "v1/chat/completions";
            }
            return url + "v1/chat/completions";
        }

        // 默认地址
        switch (provider != null ? provider : "") {
            case "zhipuai":
                return "https://open.bigmodel.cn/api/paas/v4/chat/completions";
            case "deepseek":
                return "https://api.deepseek.com/v1/chat/completions";
            case "siliconflow":
                return "https://api.siliconflow.cn/v1/chat/completions";
            default:
                return "https://api.openai.com/v1/chat/completions";
        }
    }

    private String parseResponse(String response) throws Exception {
        log.error("【DEBUG】原始响应: {}", response);
        JSONObject result = JSON.parseObject(response);

        // 检查错误
        if (result.containsKey("error")) {
            JSONObject error = result.getJSONObject("error");
            String message = error.getString("message");
            throw new RuntimeException(message != null ? message : "Unknown error");
        }

        // 尝试标准 OpenAI 格式: choices[0].message.content
        JSONArray choices = result.getJSONArray("choices");
        log.info("choices 数量: {}", choices != null ? choices.size() : 0);
        if (choices != null && !choices.isEmpty()) {
            JSONObject firstChoice = choices.getJSONObject(0);
            log.info("firstChoice: {}", firstChoice);

            // 尝试 message.content 格式
            JSONObject message = firstChoice.getJSONObject("message");
            log.info("message: {}", message);
            if (message != null) {
                String content = message.getString("content");
                if (content != null && !content.isEmpty()) {
                    return content;
                }
                // 兜底：content 为空时尝试从 reasoning_content 获取
                String reasoning = message.getString("reasoning_content");
                if (reasoning != null && !reasoning.isEmpty()) {
                    log.warn("content 为空，使用 reasoning_content 作为响应");
                    return reasoning;
                }
            }

            // 尝试 text 格式 (某些提供商使用)
            String text = firstChoice.getString("text");
            if (text != null && !text.isEmpty()) {
                return text;
            }

            // 尝试 delta.content 格式 (流式响应的非流式变体)
            JSONObject delta = firstChoice.getJSONObject("delta");
            if (delta != null) {
                String content = delta.getString("content");
                if (content != null && !content.isEmpty()) {
                    return content;
                }
            }
        }

        // 尝试直接从 result 获取 content (某些提供商的格式)
        String directContent = result.getString("content");
        if (directContent != null && !directContent.isEmpty()) {
            return directContent;
        }

        log.warn("未能从响应中提取到有效内容");
        return "AI服务响应异常";
    }

    private String extractContentFromStreamData(String data) {
        try {
            JSONObject json = JSON.parseObject(data);
            JSONArray choices = json.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject delta = firstChoice.getJSONObject("delta");
                if (delta != null) {
                    String content = delta.getString("content");
                    if (content != null) {
                        return content;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("非 JSON 内容: {}", data);
        }
        return null;
    }

    private String readErrorStream(HttpURLConnection connection) {
        try {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    errorResponse.append(line);
                }
                // 尝试解析错误信息
                try {
                    JSONObject errorJson = JSON.parseObject(errorResponse.toString());
                    if (errorJson.containsKey("error")) {
                        return errorJson.getJSONObject("error").getString("message");
                    }
                } catch (Exception ignored) {
                }
                return errorResponse.toString();
            }
        } catch (Exception e) {
            return "无法读取错误信息";
        }
    }

    private int parseContextLength(String contextLength) {
        if (contextLength == null || contextLength.isEmpty()) {
            return 8192;
        }
        switch (contextLength.toLowerCase()) {
            case "32k":
                return 32768;
            case "128k":
                return 131072;
            case "1m":
                return 1048576;
            default:
                return 8192;
        }
    }
}
