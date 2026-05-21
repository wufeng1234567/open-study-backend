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
            // 将数据提交到独立的线程池，避免阻塞主线程
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
            log.info("DeepSeek 请求体: {}", requestBody.toJSONString());
        } else if ("siliconflow".equalsIgnoreCase(provider)) {
            if (thinkingMode != null && !thinkingMode.isEmpty()) {
                boolean enableThinking = !"disabled".equals(thinkingMode);
                requestBody.put("enable_thinking", enableThinking);
                log.info("SiliconFlow 思考模式: {}", enableThinking);
            }
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();

        // 设置请求方法为 POST（用于向 AI 发送数据）
        connection.setRequestMethod("POST");

        // 设置请求头 Content-Type 为 application/json，表明发送的数据是 JSON 格式
        connection.setRequestProperty("Content-Type", "application/json");

        // 设置 Authorization 请求头，使用 Bearer 方式传递 API Key，用于身份验证
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);

        // 启用输出流，允许向连接中写入数据（即发送请求体）
        connection.setDoOutput(true);

        // 设置连接建立超时时间为 30 秒（超过此时间未建立连接则抛出异常）
        connection.setConnectTimeout(30000);

        // 设置读取响应超时时间为 120 秒（从服务器读取数据时，超过此时间无数据则抛出异常）
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
                // 因为响应的json数据可能也是不完整的 分块的，所以需要进入
                // 循环读取响应，直到读取到 null 为止
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

    // 获取ai模型的流式响应
    private void doStreamRequest(String systemPrompt, String userMessage, FluxSink<String> sink) {
        HttpURLConnection connection = null;
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
            log.info("DynamicAiClient 流式请求，URL: {}, Provider: {}, Model: {}", apiUrl, provider, model);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 4096);
            requestBody.put("stream", true);

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
                if (!extraBody.isEmpty()) {
                    requestBody.put("extra_body", extraBody);
                }
                log.info("DeepSeek 流式请求体: {}", requestBody.toJSONString());
            } else if ("siliconflow".equalsIgnoreCase(provider)) {
                if (thinkingMode != null && !thinkingMode.isEmpty()) {
                    boolean enableThinking = !"disabled".equals(thinkingMode);
                    requestBody.put("enable_thinking", enableThinking);
                    log.info("SiliconFlow 流式请求 思考模式: {}", enableThinking);
                }
            }

            connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(120000);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toJSONString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            log.info("DynamicAiClient 流式响应码: {}", responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 创建流式响应读取器BufferedReader
                try (BufferedReader reader = new BufferedReader(

                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    // 循环读取响应行，直到遇到 "[DONE]"
                    while ((line = reader.readLine()) != null) {
                        // 跳过空行
                        if (line.trim().isEmpty()) {
                            continue;
                        }
                        // 处理 SSE data 行（兼容有无空格: "data: {...}" 或 "data:{...}"）
                        if (line.startsWith("data:")) {
                            String data = line.substring(5).trim();
                            if ("[DONE]".equals(data)) {
                                break;
                            }
                            if (data.isEmpty()) {
                                continue;
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
                log.error("DynamicAiClient 流式请求失败，响应码: {}, 错误: {}", responseCode, errorResponse);
                sink.error(new RuntimeException("AI服务响应异常 (" + responseCode + "): " + errorResponse));
            }
        } catch (Exception e) {
            log.error("DynamicAiClient 流式调用失败: {}", e.getMessage(), e);
            sink.error(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String getApiUrl() {
        if (baseUrl != null && !baseUrl.isEmpty()) {
            String url = baseUrl.trim();
            if (!url.endsWith("/")) {
                url += "/";
            }

            // 智谱AI
            if ("zhipuai".equalsIgnoreCase(provider)) {
                // 如果用户已经填了完整路径（包含 /v4/），直接拼 chat/completions
                if (url.contains("/v4/")) {
                    return url + "chat/completions";
                }
                return url + "v4/chat/completions";
            }

            // DeepSeek: 统一使用 /v1/chat/completions
            if ("deepseek".equalsIgnoreCase(provider)) {
                // 如果用户填了 https://api.deepseek.com，需要加 /v1/
                // 如果用户填了 https://api.deepseek.com/v1，直接拼 chat/completions
                if (url.contains("/v1/")) {
                    return url + "chat/completions";
                }
                return url + "v1/chat/completions";
            }

            // 硅基流动、OpenAI 及其他: 统一使用 /v1/chat/completions
            if (url.contains("/v1/")) {
                return url + "chat/completions";
            }
            return url + "v1/chat/completions";
        }

        // 默认地址（无自定义 baseUrl 时使用）
        switch (provider != null ? provider.toLowerCase() : "") {
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
