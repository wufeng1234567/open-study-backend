package com.openstudy.ai.service;

import com.openstudy.ai.domain.RagDocument;

import java.util.List;

/**
 * RAG 文档服务接口
 */
public interface RagDocumentService {
    
    /**
     * 保存文档
     */
    RagDocument save(RagDocument doc);
    
    /**
     * 更新文档
     */
    int update(RagDocument doc);
    
    /**
     * 删除文档
     */
    int deleteById(Long id);
    
    /**
     * 根据ID查询文档
     */
    RagDocument getById(Long id);
    
    /**
     * 查询知识库的文档列表
     */
    List<RagDocument> listByKnowledgeBaseId(Long kbId);
    
    /**
     * 更新文档状态
     */
    int updateStatus(Long id, Integer status, String errorMsg);
    
    /**
     * 从笔记导入知识库
     * @param noteId 笔记ID
     * @param knowledgeBaseId 知识库ID
     * @return 文档ID
     */
    Long importFromNote(Long noteId, Long knowledgeBaseId);
}
