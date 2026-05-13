package com.openstudy.ai.service;

import com.openstudy.ai.domain.RagDocumentChunk;

import java.util.List;

/**
 * RAG 文档分片服务接口
 */
public interface RagDocumentChunkService {
    
    /**
     * 批量保存文档分片
     */
    int saveBatch(List<RagDocumentChunk> chunks);
    
    /**
     * 删除文档的所有分片
     */
    int deleteByDocumentId(Long documentId);
    
    /**
     * 查询文档的所有分片
     */
    List<RagDocumentChunk> listByDocumentId(Long documentId);
}
