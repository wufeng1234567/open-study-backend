package com.openstudy.ai.service.rag;

import com.openstudy.ai.service.infra.AiClient;
import com.openstudy.ai.service.infra.AiClientManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 增强聊天服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagChatService {

    private final RagDocumentService documentService;
    private final AiClientManager clientManager;

    /**
     * 基于知识库的问答
     */
    public String chatWithKnowledge(String question, String provider) {
        // 1. 检索相关文档
        List<Document> relevantDocs = documentService.searchSimilar(question, 3);

        // 2. 构建上下文
        String context = buildContext(relevantDocs);
        log.debug("检索到 {} 条相关文档", relevantDocs.size());

        // 3. 构建带上下文的提示词
        String systemPrompt = """
                你是一个知识助手，请根据提供的上下文内容回答用户的问题。
                如果上下文不包含相关信息，请如实告知用户，不要编造答案。
                
                上下文内容：
                %s
                """.formatted(context);

        String userMessage = "用户问题：" + question;

        // 4. 调用 AI 生成回答
        AiClient client = clientManager.getClient(provider);
        return client.chatWithSystem(systemPrompt, userMessage);
    }

    private String buildContext(List<Document> documents) {
        if (documents.isEmpty()) {
            return "暂无相关文档。";
        }
        return documents.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));
    }
}