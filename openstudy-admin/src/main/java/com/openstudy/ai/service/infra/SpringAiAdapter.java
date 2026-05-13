// package com.openstudy.ai.service.infra;
//
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.ai.chat.client.ChatClient;
// import org.springframework.ai.chat.model.ChatModel;
// import org.springframework.ai.chat.model.ChatResponse;
// import org.springframework.ai.chat.prompt.Prompt;
// import org.springframework.ai.chat.messages.AssistantMessage;
// import org.springframework.ai.chat.model.Generation;
// import org.springframework.ai.model.ModelOptionsUtils;
// import org.springframework.ai.chat.model.ChatOptions;
// import org.springframework.stereotype.Component;
// import reactor.core.publisher.Flux;
//
// import java.util.List;
//
// /**
//  * Spring AI 适配器 - 包装自研的 ZhipuClient
//  */
// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class SpringAiAdapter {
//
//     private final ZhipuClient zhipuClient;
//
//     /**
//      * 创建 ChatClient 实例
//      */
//     public ChatClient createChatClient() {
//         return ChatClient.builder(new ZhipuChatModelAdapter(zhipuClient)).build();
//     }
//
//     /**
//      * 适配器：将自研客户端适配为 Spring AI 的 ChatModel
//      */
//     private static class ZhipuChatModelAdapter implements ChatModel {
//
//         private final ZhipuClient zhipuClient;
//
//         public ZhipuChatModelAdapter(ZhipuClient zhipuClient) {
//             this.zhipuClient = zhipuClient;
//         }
//
//         @Override
//         public ChatResponse call(Prompt prompt) {
//             String userMessage = prompt.getInstructions().get(0).getContent();
//             log.debug("Spring AI 适配器调用，消息: {}", userMessage);
//
//             String response = zhipuClient.chat(userMessage);
//
//             Generation generation = new Generation(new AssistantMessage(response));
//             return new ChatResponse(List.of(generation));
//         }
//
//         @Override
//         public Flux<ChatResponse> stream(Prompt prompt) {
//             log.warn("流式调用暂未实现，使用普通调用");
//             return Flux.just(call(prompt));
//         }
//
//         @Override
//         public ChatOptions getDefaultOptions() {
//             // 返回空选项即可，实际由调用方覆盖
//             return ChatOptions.builder().build();
//         }
//     }
// }