package com.openstudy.ai.service.rag;

import com.openstudy.ai.domain.RagDocument;
import com.openstudy.ai.domain.RagDocumentChunk;
import com.openstudy.ai.mapper.RagDocumentChunkMapper;
import com.openstudy.ai.mapper.RagDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * RAG 向量化服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagVectorService {

    private final VectorStore vectorStore;
    private final RagDocumentChunkMapper chunkMapper;
    private final RagDocumentMapper documentMapper;

    /**
     * 对文档进行向量化（优化版：更小批次 + GC）
     */
    public void vectorizeDocument(Long documentId) {
        log.info("开始向量化文档，documentId: {}", documentId);

        // 1. 查询文档
        RagDocument doc = documentMapper.selectById(documentId);
        if (doc == null) {
            throw new RuntimeException("文档不存在");
        }

        // 2. 查询所有分块
        List<RagDocumentChunk> chunks = chunkMapper.selectByDocumentId(documentId);
        if (chunks == null || chunks.isEmpty()) {
            throw new RuntimeException("文档没有分块，请先进行分块");
        }

        // 3. 检查分块数量上限
        if (chunks.size() > 500) {
            log.error("分块数量过多: {}，超过 500 限制，跳过向量化", chunks.size());
            doc.setStatus(4);
            doc.setErrorMsg("分块数量过多: " + chunks.size() + "，超过 500 限制");
            documentMapper.update(doc);
            throw new RuntimeException("分块数量过多，无法向量化");
        }

        log.info("找到 {} 个分块，开始分批向量化", chunks.size());

        // 4. 分批处理，每批 3 个分块（从10改为3）
        int batchSize = 3;
        int totalChunks = chunks.size();
        int processedCount = 0;

        for (int i = 0; i < totalChunks; i += batchSize) {
            int endIndex = Math.min(i + batchSize, totalChunks);
            List<RagDocumentChunk> batchChunks = chunks.subList(i, endIndex);

            // 转换为 Spring AI Document
            List<Document> documents = new ArrayList<>();
            for (RagDocumentChunk chunk : batchChunks) {
                // ✅ 使用带 ID 的构造函数：id + content + metadata
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("documentId", documentId.toString());
                metadata.put("chunkIndex", String.valueOf(chunk.getChunkIndex()));
                metadata.put("knowledgeBaseId", doc.getKnowledgeBaseId().toString());
                metadata.put("userId", doc.getUserId().toString());

                // ✅ 使用带 ID 的构造函数，确保 Redis 使用这个 ID
                Document aiDoc = new Document(chunk.getVectorId(), chunk.getContent(), metadata);

                log.debug("创建向量文档: id={}, contentLength={}, metadata={}",
                        chunk.getVectorId(), chunk.getContent().length(), metadata);

                documents.add(aiDoc);
            }

            // 存储到向量数据库
            try {
                vectorStore.add(documents);
                processedCount += documents.size();

                // ✅ 添加调试：存储后立即验证
                if (!batchChunks.isEmpty()) {
                    RagDocumentChunk firstChunk = batchChunks.get(0);
                    SearchRequest verifyRequest = SearchRequest.builder()
                            .query(firstChunk.getContent().substring(0, Math.min(20, firstChunk.getContent().length())))
                            .topK(1)
                            .build();
                    List<Document> verifyResults = vectorStore.similaritySearch(verifyRequest);
                    if (verifyResults != null && !verifyResults.isEmpty()) {
                        Document first = verifyResults.get(0);
                        Map<String, Object> meta = first.getMetadata();
                        log.info("✅ 存储验证 - metadata: knowledgeBaseId={}, documentId={}, chunkIndex={}",
                                meta != null ? meta.get("knowledgeBaseId") : "null",
                                meta != null ? meta.get("documentId") : "null",
                                meta != null ? meta.get("chunkIndex") : "null");
                    }
                }

                log.info("已处理 {}/{} 个分块 (批次: {}-{})", processedCount, totalChunks, i, endIndex);
            } catch (Exception e) {
                log.error("向量化失败，批次: {}-{}", i, endIndex, e);
                throw new RuntimeException("向量化失败: " + e.getMessage(), e);
            }

            // 每批处理后清理内存并提示 GC
            documents.clear();
            System.gc();
        }

        log.info("向量化完成，共 {} 个向量", processedCount);

        // 5. 更新文档状态为已向量化（status=5）
        doc.setStatus(5);
        documentMapper.update(doc);

        log.info("文档向量化完成，documentId: {}", documentId);
    }

    /**
     * 相似度搜索
     *
     * @param query           查询文本
     * @param topK            返回最相似的 K 个结果
     * @param knowledgeBaseId 知识库ID（从 MySQL 查询验证）
     * @return 相似的分块内容列表
     */
    public List<String> searchSimilar(String query, int topK, Long knowledgeBaseId) {
        log.info("执行相似度搜索，query: {}, topK: {}, knowledgeBaseId: {}", query, topK, knowledgeBaseId);

        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(topK * 3)
                    .build();

            List<Document> results = vectorStore.similaritySearch(searchRequest);
            log.info("向量搜索原始结果数量: {}", results.size());

            // ✅ 优化：批量查询 MySQL 替代 N+1
            List<Document> filtered = new ArrayList<>();
            if (results != null && !results.isEmpty()) {
                List<String> vectorIds = results.stream()
                        .map(Document::getId)
                        .collect(Collectors.toList());

                // 1. 批量查询所有 vectorId 对应的 chunk
                List<RagDocumentChunk> chunks = chunkMapper.selectByVectorIds(vectorIds);
                Map<String, Long> chunkDocMap = chunks.stream()
                        .collect(Collectors.toMap(RagDocumentChunk::getVectorId, RagDocumentChunk::getDocumentId));

                // 2. 批量查询所有涉及的 document
                List<Long> docIds = chunks.stream()
                        .map(RagDocumentChunk::getDocumentId)
                        .distinct()
                        .collect(Collectors.toList());
                List<RagDocument> docs = docIds.isEmpty() ? Collections.emptyList() : documentMapper.selectByIds(docIds);
                Map<Long, Long> docKbMap = docs.stream()
                        .collect(Collectors.toMap(RagDocument::getId, RagDocument::getKnowledgeBaseId));

                // 3. 内存中过滤
                for (Document doc : results) {
                    String vId = doc.getId();
                    Long docId = chunkDocMap.get(vId);
                    if (docId != null) {
                        Long kbId = docKbMap.get(docId);
                        if (kbId != null && kbId.equals(knowledgeBaseId)) {
                            filtered.add(doc);
                            log.debug("✅ 匹配成功: vectorId={}, documentId={}, knowledgeBaseId={}", vId, docId, kbId);
                        } else {
                            log.debug("❌ 不匹配: vectorId={}, docKBId={}, targetKBId={}", vId, kbId, knowledgeBaseId);
                        }
                    } else {
                        log.debug("❌ 未找到 chunk: vectorId={}", vId);
                    }
                    if (filtered.size() >= topK) break;
                }
            }

            log.info("搜索完成，原始 {} 个结果，过滤后 {} 个结果", results.size(), filtered.size());

            List<String> contents = new ArrayList<>();
            for (Document doc : filtered) {
                contents.add(doc.getContent());
            }
            return contents;
        } catch (Exception e) {
            log.error("相似度搜索失败", e);
            throw new RuntimeException("相似度搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 带分数的相似度搜索（用于越界检测）
     *
     * @param query           查询文本
     * @param topK            返回最相似的 K 个结果
     * @param knowledgeBaseId 知识库ID（Java 侧过滤）
     * @return Document 列表，包含 metadata 中的分数信息
     */
    public List<Document> searchSimilarWithScore(String query, int topK, Long knowledgeBaseId) {
        log.info("执行带分数的相似度搜索，query: {}, topK: {}, knowledgeBaseId: {}", query, topK, knowledgeBaseId);

        try {
            // 搜索时不过滤 knowledgeBaseId，取更多结果后在 Java 侧过滤
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(topK * 3)
                    .build();

            List<Document> results = vectorStore.similaritySearch(searchRequest);

            log.info("向量搜索原始结果数量: {}", results != null ? results.size() : "null");

            // ✅ 优化：批量查询 MySQL 替代 N+1
            List<Document> filtered = new ArrayList<>();
            if (results != null && !results.isEmpty()) {
                List<String> vectorIds = results.stream()
                        .map(Document::getId)
                        .collect(Collectors.toList());

                // 1. 批量查询所有 vectorId 对应的 chunk
                List<RagDocumentChunk> chunks = chunkMapper.selectByVectorIds(vectorIds);
                Map<String, Long> chunkDocMap = chunks.stream()
                        .collect(Collectors.toMap(RagDocumentChunk::getVectorId, RagDocumentChunk::getDocumentId));

                // 2. 批量查询所有涉及的 document
                List<Long> docIds = chunks.stream()
                        .map(RagDocumentChunk::getDocumentId)
                        .distinct()
                        .collect(Collectors.toList());
                List<RagDocument> docs = docIds.isEmpty() ? Collections.emptyList() : documentMapper.selectByIds(docIds);
                Map<Long, Long> docKbMap = docs.stream()
                        .collect(Collectors.toMap(RagDocument::getId, RagDocument::getKnowledgeBaseId));

                // 3. 内存中过滤
                for (Document doc : results) {
                    String vId = doc.getId();
                    Long docId = chunkDocMap.get(vId);
                    if (docId != null) {
                        Long kbId = docKbMap.get(docId);
                        if (kbId != null && kbId.equals(knowledgeBaseId)) {
                            filtered.add(doc);
                            log.debug("✅ 匹配成功: vectorId={}, documentId={}, knowledgeBaseId={}", vId, docId, kbId);
                        } else {
                            log.debug("❌ 不匹配: vectorId={}, docKBId={}, targetKBId={}", vId, kbId, knowledgeBaseId);
                        }
                    } else {
                        log.debug("❌ 未找到 chunk: vectorId={}", vId);
                    }
                    if (filtered.size() >= topK) break;
                }
            }

            log.info("搜索完成，原始 {} 个结果，过滤后 {} 个结果",
                    results != null ? results.size() : 0, filtered.size());

            // 输出每个结果的相似度得分（调试用）
            for (int i = 0; i < filtered.size(); i++) {
                Document doc = filtered.get(i);
                Double score = extractScore(doc);
                log.debug("结果 {}: score={}, content={}",
                        i, score,
                        doc.getContent().substring(0, Math.min(50, doc.getContent().length())));
            }

            return filtered;
        } catch (Exception e) {
            log.error("带分数的相似度搜索失败", e);
            throw new RuntimeException("相似度搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从 Document 中提取相似度分数
     * Spring AI RedisVectorStore 可能使用 "score" 或 "distance" 字段
     *
     * @param document Spring AI Document
     * @return 相似度分数（0-1之间，越大越相似），如果无法提取则返回 0.0
     */
    private Double extractScore(Document document) {
        if (document == null || document.getMetadata() == null) {
            return 0.0;
        }

        Map<String, Object> metadata = document.getMetadata();

        // 尝试获取 "score" 字段（余弦相似度）
        if (metadata.containsKey("score")) {
            Object scoreObj = metadata.get("score");
            if (scoreObj instanceof Number) {
                return ((Number) scoreObj).doubleValue();
            }
        }

        // 尝试获取 "distance" 字段（欧氏距离，需要转换）
        if (metadata.containsKey("distance")) {
            Object distanceObj = metadata.get("distance");
            if (distanceObj instanceof Number) {
                double distance = ((Number) distanceObj).doubleValue();
                // 转换公式：similarity = 1 / (1 + distance)
                return 1.0 / (1.0 + distance);
            }
        }

        // 如果没有找到分数，返回默认值
        log.warn("无法从 Document metadata 中提取分数，使用默认值 0.0");
        return 0.0;
    }

    /**
     * 删除文档的所有向量数据
     *
     * @param documentId 文档ID
     */
    public void deleteByDocumentId(Long documentId) {
        log.info("开始删除文档向量，documentId: {}", documentId);

        // 1. 查询该文档的所有分块
        List<RagDocumentChunk> chunks = chunkMapper.selectByDocumentId(documentId);

        if (chunks == null || chunks.isEmpty()) {
            log.warn("文档没有分块，无需删除向量: documentId={}", documentId);
            return;
        }

        log.info("找到 {} 个分块，准备删除向量", chunks.size());

        // 2. 构建向量 ID 列表
        List<String> vectorIds = chunks.stream()
                .map(RagDocumentChunk::getVectorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!vectorIds.isEmpty()) {
            log.info("提取到 {} 个向量ID: {}", vectorIds.size(), vectorIds);

            try {
                // 3. 调用 VectorStore 删除
                vectorStore.delete(vectorIds);
                log.info("✅ 已成功删除 {} 个向量，documentId: {}", vectorIds.size(), documentId);

                // 4. 验证删除结果（可选）
                for (String vectorId : vectorIds) {
                    log.debug("已删除向量: {}", vectorId);
                }
            } catch (Exception e) {
                log.error("❌ 删除向量失败，documentId: {}, vectorIds: {}", documentId, vectorIds, e);
                throw new RuntimeException("删除向量失败: " + e.getMessage(), e);
            }
        } else {
            log.warn("没有找到有效的向量ID，documentId: {}", documentId);
        }
    }
}
