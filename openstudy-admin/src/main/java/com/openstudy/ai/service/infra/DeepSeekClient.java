package com.openstudy.ai.service.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * DeepSeek 客户端（通过 Spring AI OpenAI 兼容接口）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeepSeekClient implements AiClient {

    @Qualifier("deepseekChatModel")
    private final ChatModel deepseekChatModel;

    @Override
    public String chat(String message) {
        try {
            Prompt prompt = new Prompt(message);
            return deepseekChatModel.call(prompt)
                    .getResult()
                    .getOutput()
                    .getContent();
        } catch (Exception e) {
            log.error("DeepSeek 调用失败", e);
            return "DeepSeek 服务暂时不可用：" + e.getMessage();
        }
    }

    @Override
    public String chatWithSystem(String systemPrompt, String userMessage) {
        try {
            // 构建包含 system 和 user 消息的 Prompt
            org.springframework.ai.chat.messages.Message systemMsg =
                    new org.springframework.ai.chat.messages.SystemMessage(systemPrompt);
            org.springframework.ai.chat.messages.Message userMsg =
                    new org.springframework.ai.chat.messages.UserMessage(userMessage);

            Prompt prompt = new Prompt(java.util.List.of(systemMsg, userMsg));
            return deepseekChatModel.call(prompt)
                    .getResult()
                    .getOutput()
                    .getContent();
        } catch (Exception e) {
            log.error("DeepSeek 带系统提示词调用失败", e);
            return "DeepSeek 服务暂时不可用：" + e.getMessage();
        }
    }

    @Override
    public Flux<String> chatStream(String systemPrompt, String userMessage) {
        // 流式调用后续再实现，暂时返回非流式结果
        log.warn("DeepSeek 流式暂未实现，使用普通调用");
        return Flux.just(chatWithSystem(systemPrompt, userMessage));
    }

    @Override
    public String getProviderName() {
        return "deepseek";
    }

    @Override
    public boolean isAvailable() {
        return true;  // 现在可用
    }
}