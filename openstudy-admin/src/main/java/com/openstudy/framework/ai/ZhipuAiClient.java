package com.openstudy.framework.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

@Slf4j
@Component
public class ZhipuAiClient {

    @Value("${spring.ai.zhipuai.api-key:efc8e97fe926492eb11e0919055ca724.1S2c175Vkwp8yxQJ}")
    private String apiKey;

    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private static final String DEFAULT_MODEL = "glm-4-plus";

    // 用于异步执行阻塞 I/O 的线程池
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public String chat(String message) {
        return chat(message, DEFAULT_MODEL);
    }

    public String chat(String message, String model) {
        return chatWithSystem(null, message, model);
    }

    public String chatWithSystem(String systemPrompt, String userMessage, String model) {
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

            return doRequest(messages, model);
        } catch (Exception e) {
            log.error("智谱AI调用失败", e);
            return "AI服务暂时不可用：" + e.getMessage();
        }
    }

    /**
     * 流式聊天（SSE）- 优化版
     */
    public Flux<String> chatStream(String systemPrompt, String userMessage, String model) {
        return Flux.create(sink -> {
            executor.submit(() -> {
                try {
                    doStreamRequest(systemPrompt, userMessage, model, sink);
                } catch (Exception e) {
                    log.error("智谱AI流式调用失败", e);
                    sink.error(e);
                }
            });
        });
    }

    private void doStreamRequest(String systemPrompt, String userMessage, String model, FluxSink<String> sink) {
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

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model != null ? model : DEFAULT_MODEL);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2048);
            requestBody.put("stream", true);

            log.info("流式请求智谱AI，URL: {}", API_URL);

            HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(120000);

            // 发送请求体
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toJSONString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    StringBuilder buffer = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        // 智谱AI 的 SSE 格式: "data: {...}"
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6).trim();

                            if ("[DONE]".equals(data)) {
                                log.info("流式响应完成");
                                break;
                            }

                            // 尝试解析并推送内容
                            String content = extractContentFromStreamData(data);
                            if (content != null && !content.isEmpty()) {
                                sink.next(content);
                            }
                        }
                    }
                }
                sink.complete();
            } else {
                // 读取错误响应
                StringBuilder errorResponse = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                }
                log.error("智谱AI流式请求失败，响应码: {}, 错误: {}", responseCode, errorResponse);
                sink.error(new RuntimeException("AI服务响应异常: " + errorResponse));
            }
        } catch (Exception e) {
            log.error("智谱AI流式调用失败", e);
            sink.error(e);
        }
    }

    /**
     * 从流式数据中提取内容片段
     */
    private String extractContentFromStreamData(String data) {
        try {
            JSONObject json = JSON.parseObject(data);
            JSONArray choices = json.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject firstChoice = choices.getJSONObject(0);

                // 优先从 delta 中获取 content
                JSONObject delta = firstChoice.getJSONObject("delta");
                if (delta != null) {
                    String content = delta.getString("content");
                    if (content != null) {
                        return content;
                    }
                }
            }
        } catch (Exception e) {
            // 忽略解析错误，可能是 [DONE] 或其他非 JSON 内容
            log.debug("非 JSON 内容: {}", data);
        }
        return null;
    }

    private String doRequest(List<Map<String, String>> messages, String model) throws Exception {
        log.info("请求智谱AI，URL: {}", API_URL);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 4096);
        requestBody.put("stream", false);

        HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(120000);  // 统一改为 120 秒

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toJSONString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        log.info("响应码: {}", responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                JSONObject result = JSON.parseObject(response.toString());
                JSONArray choices = result.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                    if (message != null) {
                        return message.getString("content");
                    }
                }
            }
        } else {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    errorResponse.append(line);
                }
                log.error("智谱AI请求失败，响应码: {}, 错误: {}", responseCode, errorResponse);
                return "AI服务响应异常: " + errorResponse;
            }
        }

        return "AI服务响应异常";
    }
}