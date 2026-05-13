package com.openstudy.ai.service.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 硅基流动客户端（通过 Spring AI OpenAI 兼容接口）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SiliconflowClient implements AiClient {

    @Qualifier("siliconflowChatModel")
    private final ChatModel siliconflowChatModel;

    @Override
    public String chat(String message) {
        try {
            Prompt prompt = new Prompt(message);
            return siliconflowChatModel.call(prompt)
                    .getResult()
                    .getOutput()
                    .getContent();
        } catch (Exception e) {
            log.error("硅基流动调用失败", e);
            return "硅基流动服务暂时不可用：" + e.getMessage();
        }
    }

    @Override
    public String chatWithSystem(String systemPrompt, String userMessage) {
        try {
            org.springframework.ai.chat.messages.Message systemMsg =
                    new org.springframework.ai.chat.messages.SystemMessage(systemPrompt);
            org.springframework.ai.chat.messages.Message userMsg =
                    new org.springframework.ai.chat.messages.UserMessage(userMessage);

            Prompt prompt = new Prompt(java.util.List.of(systemMsg, userMsg));
            return siliconflowChatModel.call(prompt)
                    .getResult()
                    .getOutput()
                    .getContent();
        } catch (Exception e) {
            log.error("硅基流动带系统提示词调用失败", e);
            return "硅基流动服务暂时不可用：" + e.getMessage();
        }
    }

    @Override
    public Flux<String> chatStream(String systemPrompt, String userMessage) {
        log.warn("硅基流动流式暂未实现，使用普通调用");
        return Flux.just(chatWithSystem(systemPrompt, userMessage));
    }

    @Override
    public String getProviderName() {
        return "siliconflow";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}