package com.openstudy.ai.service.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * RAG 文档管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagDocumentService {

    private final VectorStore vectorStore;

    /**
     * 添加文档到向量库
     */
    public void addDocument(String content, Map<String, Object> metadata) {
        Document document = new Document(content, metadata);
        vectorStore.add(List.of(document));
        log.info("文档已添加到向量库，元数据: {}", metadata);
    }

    /**
     * 批量添加文档
     */
    public void addDocuments(List<Document> documents) {
        vectorStore.add(documents);
        log.info("批量添加 {} 个文档到向量库", documents.size());
    }

    /**
     * 根据相似度检索文档片段
     */
    public List<Document> searchSimilar(String query, int topK) {
        // 使用新的 API
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .build()
        );
    }
}